package com.accounting.repository;

import com.accounting.model.Account;
import com.accounting.model.AccountType;
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
@DisplayName("AccountRepository Integration Tests")
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    private Account cashAccount;
    private Account arAccount;
    private Account apAccount;
    private Account revenueAccount;
    private Account expenseAccount;

    @BeforeEach
    void setUp() {
        cashAccount = createAndSaveAccount("1000", "Cash", AccountType.ASSET, true);
        arAccount = createAndSaveAccount("1200", "Accounts Receivable", AccountType.ASSET, true);
        apAccount = createAndSaveAccount("2000", "Accounts Payable", AccountType.LIABILITY, true);
        revenueAccount = createAndSaveAccount("4000", "Sales Revenue", AccountType.REVENUE, true);
        expenseAccount = createAndSaveAccount("5000", "Operating Expenses", AccountType.EXPENSE, false);
        entityManager.flush();
        entityManager.clear();
    }

    private Account createAndSaveAccount(String code, String name, AccountType type, boolean isActive) {
        Account account = new Account();
        account.setCode(code);
        account.setName(name);
        account.setAccountType(type);
        account.setIsActive(isActive);
        return entityManager.persist(account);
    }

    @Nested
    @DisplayName("Find By Code")
    class FindByCode {

        @Test
        @DisplayName("Should find account by code when exists")
        void findByCode_WhenExists_ReturnsAccount() {
            Optional<Account> result = accountRepository.findByCode("1000");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Cash");
        }

        @Test
        @DisplayName("Should return empty when code does not exist")
        void findByCode_WhenNotExists_ReturnsEmpty() {
            Optional<Account> result = accountRepository.findByCode("9999");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Exists By Code")
    class ExistsByCode {

        @Test
        @DisplayName("Should return true when code exists")
        void existsByCode_WhenExists_ReturnsTrue() {
            boolean result = accountRepository.existsByCode("1000");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when code does not exist")
        void existsByCode_WhenNotExists_ReturnsFalse() {
            boolean result = accountRepository.existsByCode("9999");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Find By Account Type")
    class FindByAccountType {

        @Test
        @DisplayName("Should find all asset accounts")
        void findByAccountType_Asset_ReturnsAssetAccounts() {
            List<Account> result = accountRepository.findByAccountType(AccountType.ASSET);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(a -> a.getAccountType() == AccountType.ASSET);
        }

        @Test
        @DisplayName("Should find all liability accounts")
        void findByAccountType_Liability_ReturnsLiabilityAccounts() {
            List<Account> result = accountRepository.findByAccountType(AccountType.LIABILITY);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCode()).isEqualTo("2000");
        }
    }

    @Nested
    @DisplayName("Find All Active")
    class FindAllActive {

        @Test
        @DisplayName("Should find only active accounts")
        void findAllActive_ReturnsOnlyActiveAccounts() {
            List<Account> result = accountRepository.findAllActive();

            assertThat(result).hasSize(4);
            assertThat(result).allMatch(Account::getIsActive);
        }

        @Test
        @DisplayName("Should return accounts ordered by code")
        void findAllActive_ReturnsAccountsOrderedByCode() {
            List<Account> result = accountRepository.findAllActive();

            assertThat(result.get(0).getCode()).isEqualTo("1000");
            assertThat(result.get(1).getCode()).isEqualTo("1200");
            assertThat(result.get(2).getCode()).isEqualTo("2000");
        }
    }

    @Nested
    @DisplayName("Find Active By Type")
    class FindActiveByType {

        @Test
        @DisplayName("Should find only active accounts of specified type")
        void findActiveByType_ReturnsActiveAccountsOfType() {
            List<Account> result = accountRepository.findActiveByType(AccountType.ASSET);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(a -> a.getIsActive() && a.getAccountType() == AccountType.ASSET);
        }

        @Test
        @DisplayName("Should not include inactive accounts")
        void findActiveByType_ExcludesInactiveAccounts() {
            List<Account> result = accountRepository.findActiveByType(AccountType.EXPENSE);

            assertThat(result).isEmpty(); // Expense account is inactive
        }
    }

    @Nested
    @DisplayName("Hierarchical Account Structure")
    class HierarchicalAccountStructure {

        @Test
        @DisplayName("Should find top level accounts")
        void findTopLevelAccounts_ReturnsAccountsWithNoParent() {
            List<Account> result = accountRepository.findTopLevelAccounts();

            assertThat(result).hasSize(5);
            assertThat(result).allMatch(a -> a.getParent() == null);
        }

        @Test
        @DisplayName("Should find child accounts by parent ID")
        void findByParentId_ReturnsChildAccounts() {
            // Create a child account
            Account pettyCash = new Account();
            pettyCash.setCode("1001");
            pettyCash.setName("Petty Cash");
            pettyCash.setAccountType(AccountType.ASSET);
            pettyCash.setIsActive(true);
            pettyCash.setParent(cashAccount);
            entityManager.persist(pettyCash);
            entityManager.flush();
            entityManager.clear();

            List<Account> result = accountRepository.findByParentId(cashAccount.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCode()).isEqualTo("1001");
        }
    }

    @Nested
    @DisplayName("Find Distinct Account Types")
    class FindDistinctAccountTypes {

        @Test
        @DisplayName("Should find distinct account types from active accounts")
        void findDistinctAccountTypes_ReturnsDistinctTypes() {
            List<AccountType> result = accountRepository.findDistinctAccountTypes();

            // Only 3 distinct active account types: ASSET, LIABILITY, REVENUE (EXPENSE is inactive)
            assertThat(result).hasSize(3);
            assertThat(result).contains(AccountType.ASSET, AccountType.LIABILITY, AccountType.REVENUE);
        }
    }
}