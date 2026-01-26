package com.accounting.repository;

import com.accounting.model.Account;
import com.accounting.model.AccountType;
import com.accounting.model.BankAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BankAccountRepository Integration Tests")
class BankAccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private Account cashAccount;
    private Account savingsGlAccount;
    private BankAccount checkingAccount;
    private BankAccount savingsAccount;
    private BankAccount inactiveAccount;

    @BeforeEach
    void setUp() {
        // Create GL accounts
        cashAccount = createGlAccount("1000", "Cash", AccountType.ASSET);
        savingsGlAccount = createGlAccount("1010", "Savings", AccountType.ASSET);
        entityManager.persist(cashAccount);
        entityManager.persist(savingsGlAccount);

        // Create bank accounts
        checkingAccount = createBankAccount("Main Checking", "123456789", "First Bank", cashAccount, true);
        savingsAccount = createBankAccount("Savings Account", "987654321", "First Bank", savingsGlAccount, true);
        inactiveAccount = createBankAccount("Old Account", "111222333", "Old Bank", cashAccount, false);

        entityManager.persist(checkingAccount);
        entityManager.persist(savingsAccount);
        entityManager.persist(inactiveAccount);
        entityManager.flush();
        entityManager.clear();
    }

    private Account createGlAccount(String code, String name, AccountType type) {
        Account account = new Account();
        account.setCode(code);
        account.setName(name);
        account.setAccountType(type);
        account.setIsActive(true);
        return account;
    }

    private BankAccount createBankAccount(String name, String accountNumber, String bankName,
                                          Account glAccount, boolean isActive) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountName(name);
        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setBankName(bankName);
        bankAccount.setGlAccount(glAccount);
        bankAccount.setIsActive(isActive);
        return bankAccount;
    }

    @Nested
    @DisplayName("Find By Account Number")
    class FindByAccountNumber {

        @Test
        @DisplayName("Should find bank account by account number")
        void findByAccountNumber_WhenExists_ReturnsBankAccount() {
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber("123456789");

            assertThat(result).isPresent();
            assertThat(result.get().getAccountName()).isEqualTo("Main Checking");
        }

        @Test
        @DisplayName("Should return empty when account number does not exist")
        void findByAccountNumber_WhenNotExists_ReturnsEmpty() {
            Optional<BankAccount> result = bankAccountRepository.findByAccountNumber("000000000");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should check if account number exists")
        void existsByAccountNumber_ReturnsCorrectResult() {
            assertThat(bankAccountRepository.existsByAccountNumber("123456789")).isTrue();
            assertThat(bankAccountRepository.existsByAccountNumber("000000000")).isFalse();
        }
    }

    @Nested
    @DisplayName("Find All Active")
    class FindAllActive {

        @Test
        @DisplayName("Should find only active bank accounts")
        void findAllActive_ReturnsOnlyActiveAccounts() {
            List<BankAccount> result = bankAccountRepository.findAllActive();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(BankAccount::getIsActive)
                    .containsOnly(true);
            assertThat(result).extracting(BankAccount::getAccountName)
                    .containsExactlyInAnyOrder("Main Checking", "Savings Account");
        }
    }

    @Nested
    @DisplayName("Find By GL Account")
    class FindByGlAccount {

        @Test
        @DisplayName("Should find bank accounts by GL account ID")
        void findByGlAccountId_ReturnsMatchingAccounts() {
            List<BankAccount> result = bankAccountRepository.findByGlAccountId(cashAccount.getId());

            assertThat(result).hasSize(2);
            assertThat(result).extracting(BankAccount::getAccountName)
                    .containsExactlyInAnyOrder("Main Checking", "Old Account");
        }

        @Test
        @DisplayName("Should return single account when only one linked to GL account")
        void findByGlAccountId_SingleMatch_ReturnsSingleAccount() {
            List<BankAccount> result = bankAccountRepository.findByGlAccountId(savingsGlAccount.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAccountName()).isEqualTo("Savings Account");
        }

        @Test
        @DisplayName("Should return empty list when no bank accounts linked to GL account")
        void findByGlAccountId_NoMatch_ReturnsEmptyList() {
            Account newGlAccount = createGlAccount("1020", "Petty Cash", AccountType.ASSET);
            entityManager.persist(newGlAccount);
            entityManager.flush();

            List<BankAccount> result = bankAccountRepository.findByGlAccountId(newGlAccount.getId());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save new bank account")
        void save_NewBankAccount_PersistsSuccessfully() {
            BankAccount newAccount = createBankAccount("New Account", "555666777",
                    "New Bank", cashAccount, true);

            BankAccount savedAccount = bankAccountRepository.save(newAccount);

            assertThat(savedAccount.getId()).isNotNull();
            assertThat(bankAccountRepository.count()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should update existing bank account")
        void save_ExistingBankAccount_UpdatesSuccessfully() {
            BankAccount existingAccount = bankAccountRepository.findByAccountNumber("123456789").get();
            existingAccount.setBankName("Updated Bank Name");

            bankAccountRepository.save(existingAccount);
            entityManager.flush();
            entityManager.clear();

            BankAccount updatedAccount = bankAccountRepository.findByAccountNumber("123456789").get();
            assertThat(updatedAccount.getBankName()).isEqualTo("Updated Bank Name");
        }

        @Test
        @DisplayName("Should delete bank account")
        void delete_ExistingBankAccount_RemovesSuccessfully() {
            BankAccount accountToDelete = bankAccountRepository.findByAccountNumber("111222333").get();

            bankAccountRepository.delete(accountToDelete);
            entityManager.flush();

            assertThat(bankAccountRepository.findByAccountNumber("111222333")).isEmpty();
            assertThat(bankAccountRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should find all bank accounts")
        void findAll_ReturnsAllBankAccounts() {
            List<BankAccount> result = bankAccountRepository.findAll();

            assertThat(result).hasSize(3);
        }
    }
}