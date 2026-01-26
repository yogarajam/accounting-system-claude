package com.accounting.service;

import com.accounting.exception.AccountingException;
import com.accounting.model.Account;
import com.accounting.model.AccountType;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.JournalEntryLineRepository;
import com.accounting.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;

    @InjectMocks
    private AccountService accountService;

    private Account cashAccount;
    private Account revenueAccount;
    private Account liabilityAccount;

    @BeforeEach
    void setUp() {
        cashAccount = TestDataBuilder.createCashAccount();
        revenueAccount = TestDataBuilder.createSalesRevenue();
        liabilityAccount = TestDataBuilder.createAccountsPayable();
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should find all accounts")
        void findAll_ReturnsAllAccounts() {
            List<Account> accounts = Arrays.asList(cashAccount, revenueAccount);
            when(accountRepository.findAll()).thenReturn(accounts);

            List<Account> result = accountService.findAll();

            assertThat(result).hasSize(2);
            verify(accountRepository).findAll();
        }

        @Test
        @DisplayName("Should find all active accounts")
        void findAllActive_ReturnsOnlyActiveAccounts() {
            List<Account> accounts = Arrays.asList(cashAccount, revenueAccount);
            when(accountRepository.findAllActive()).thenReturn(accounts);

            List<Account> result = accountService.findAllActive();

            assertThat(result).hasSize(2);
            verify(accountRepository).findAllActive();
        }

        @Test
        @DisplayName("Should find account by ID")
        void findById_WhenExists_ReturnsAccount() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));

            Optional<Account> result = accountService.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("1000");
        }

        @Test
        @DisplayName("Should return empty when account not found by ID")
        void findById_WhenNotExists_ReturnsEmpty() {
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Account> result = accountService.findById(99L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find account by code")
        void findByCode_WhenExists_ReturnsAccount() {
            when(accountRepository.findByCode("1000")).thenReturn(Optional.of(cashAccount));

            Optional<Account> result = accountService.findByCode("1000");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Cash");
        }

        @Test
        @DisplayName("Should find accounts by type")
        void findByType_ReturnsMatchingAccounts() {
            List<Account> assetAccounts = Arrays.asList(cashAccount);
            when(accountRepository.findByAccountType(AccountType.ASSET)).thenReturn(assetAccounts);

            List<Account> result = accountService.findByType(AccountType.ASSET);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAccountType()).isEqualTo(AccountType.ASSET);
        }

        @Test
        @DisplayName("Should find active accounts by type")
        void findActiveByType_ReturnsActiveMatchingAccounts() {
            List<Account> revenueAccounts = Arrays.asList(revenueAccount);
            when(accountRepository.findActiveByType(AccountType.REVENUE)).thenReturn(revenueAccounts);

            List<Account> result = accountService.findActiveByType(AccountType.REVENUE);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should find top level accounts")
        void findTopLevelAccounts_ReturnsAccountsWithNoParent() {
            List<Account> topLevel = Arrays.asList(cashAccount, revenueAccount);
            when(accountRepository.findTopLevelAccounts()).thenReturn(topLevel);

            List<Account> result = accountService.findTopLevelAccounts();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should find accounts by parent ID")
        void findByParentId_ReturnsChildAccounts() {
            Account childAccount = TestDataBuilder.createAccount(10L, "1001", "Petty Cash", AccountType.ASSET);
            when(accountRepository.findByParentId(1L)).thenReturn(Arrays.asList(childAccount));

            List<Account> result = accountService.findByParentId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCode()).isEqualTo("1001");
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {

        @Test
        @DisplayName("Should save new account successfully")
        void save_NewAccount_SavesSuccessfully() {
            Account newAccount = TestDataBuilder.createAccount(null, "1100", "Bank", AccountType.ASSET);
            Account savedAccount = TestDataBuilder.createAccount(5L, "1100", "Bank", AccountType.ASSET);

            when(accountRepository.existsByCode("1100")).thenReturn(false);
            when(accountRepository.save(newAccount)).thenReturn(savedAccount);

            Account result = accountService.save(newAccount);

            assertThat(result.getId()).isEqualTo(5L);
            verify(accountRepository).existsByCode("1100");
            verify(accountRepository).save(newAccount);
        }

        @Test
        @DisplayName("Should throw exception when saving account with duplicate code")
        void save_DuplicateCode_ThrowsException() {
            Account newAccount = TestDataBuilder.createAccount(null, "1000", "Duplicate", AccountType.ASSET);
            when(accountRepository.existsByCode("1000")).thenReturn(true);

            assertThatThrownBy(() -> accountService.save(newAccount))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Account code already exists");

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update existing account without code check")
        void save_ExistingAccount_UpdatesWithoutCodeCheck() {
            cashAccount.setName("Updated Cash");
            when(accountRepository.save(cashAccount)).thenReturn(cashAccount);

            Account result = accountService.save(cashAccount);

            assertThat(result.getName()).isEqualTo("Updated Cash");
            verify(accountRepository, never()).existsByCode(any());
        }
    }

    @Nested
    @DisplayName("Activate/Deactivate Operations")
    class ActivationOperations {

        @Test
        @DisplayName("Should deactivate account with zero balance")
        void deactivate_ZeroBalance_DeactivatesSuccessfully() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountId(1L)).thenReturn(BigDecimal.valueOf(100));
            when(journalEntryLineRepository.sumCreditByAccountId(1L)).thenReturn(BigDecimal.valueOf(100));
            when(accountRepository.save(any(Account.class))).thenReturn(cashAccount);

            accountService.deactivate(1L);

            assertThat(cashAccount.getIsActive()).isFalse();
            verify(accountRepository).save(cashAccount);
        }

        @Test
        @DisplayName("Should throw exception when deactivating account with non-zero balance")
        void deactivate_NonZeroBalance_ThrowsException() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountId(1L)).thenReturn(BigDecimal.valueOf(100));
            when(journalEntryLineRepository.sumCreditByAccountId(1L)).thenReturn(BigDecimal.valueOf(50));

            assertThatThrownBy(() -> accountService.deactivate(1L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Cannot deactivate account with non-zero balance");

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when deactivating non-existent account")
        void deactivate_AccountNotFound_ThrowsException() {
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.deactivate(99L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        @DisplayName("Should activate inactive account")
        void activate_InactiveAccount_ActivatesSuccessfully() {
            cashAccount.setIsActive(false);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(cashAccount);

            accountService.activate(1L);

            assertThat(cashAccount.getIsActive()).isTrue();
            verify(accountRepository).save(cashAccount);
        }

        @Test
        @DisplayName("Should throw exception when activating non-existent account")
        void activate_AccountNotFound_ThrowsException() {
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.activate(99L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Account not found");
        }
    }

    @Nested
    @DisplayName("Balance Calculations")
    class BalanceCalculations {

        @Test
        @DisplayName("Should calculate balance for debit-normal account (Assets/Expenses)")
        void getBalance_DebitNormalAccount_ReturnsDebitMinusCredit() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountId(1L)).thenReturn(BigDecimal.valueOf(1000));
            when(journalEntryLineRepository.sumCreditByAccountId(1L)).thenReturn(BigDecimal.valueOf(300));

            BigDecimal balance = accountService.getBalance(1L);

            assertThat(balance).isEqualByComparingTo(BigDecimal.valueOf(700));
        }

        @Test
        @DisplayName("Should calculate balance for credit-normal account (Liabilities/Revenue)")
        void getBalance_CreditNormalAccount_ReturnsCreditMinusDebit() {
            when(accountRepository.findById(4L)).thenReturn(Optional.of(revenueAccount));
            when(journalEntryLineRepository.sumDebitByAccountId(4L)).thenReturn(BigDecimal.valueOf(100));
            when(journalEntryLineRepository.sumCreditByAccountId(4L)).thenReturn(BigDecimal.valueOf(500));

            BigDecimal balance = accountService.getBalance(4L);

            assertThat(balance).isEqualByComparingTo(BigDecimal.valueOf(400));
        }

        @Test
        @DisplayName("Should calculate balance as of date for debit-normal account")
        void getBalanceAsOfDate_DebitNormalAccount_CalculatesCorrectly() {
            LocalDate asOfDate = LocalDate.now();
            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountIdBetweenDates(eq(1L), any(), eq(asOfDate)))
                    .thenReturn(BigDecimal.valueOf(2000));
            when(journalEntryLineRepository.sumCreditByAccountIdBetweenDates(eq(1L), any(), eq(asOfDate)))
                    .thenReturn(BigDecimal.valueOf(500));

            BigDecimal balance = accountService.getBalanceAsOfDate(1L, asOfDate);

            assertThat(balance).isEqualByComparingTo(BigDecimal.valueOf(1500));
        }

        @Test
        @DisplayName("Should calculate balance as of date for credit-normal account")
        void getBalanceAsOfDate_CreditNormalAccount_CalculatesCorrectly() {
            LocalDate asOfDate = LocalDate.now();
            when(accountRepository.findById(3L)).thenReturn(Optional.of(liabilityAccount));
            when(journalEntryLineRepository.sumDebitByAccountIdBetweenDates(eq(3L), any(), eq(asOfDate)))
                    .thenReturn(BigDecimal.valueOf(200));
            when(journalEntryLineRepository.sumCreditByAccountIdBetweenDates(eq(3L), any(), eq(asOfDate)))
                    .thenReturn(BigDecimal.valueOf(800));

            BigDecimal balance = accountService.getBalanceAsOfDate(3L, asOfDate);

            assertThat(balance).isEqualByComparingTo(BigDecimal.valueOf(600));
        }

        @Test
        @DisplayName("Should calculate balance between dates")
        void getBalanceBetweenDates_CalculatesCorrectly() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();
            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(journalEntryLineRepository.sumDebitByAccountIdBetweenDates(1L, startDate, endDate))
                    .thenReturn(BigDecimal.valueOf(5000));
            when(journalEntryLineRepository.sumCreditByAccountIdBetweenDates(1L, startDate, endDate))
                    .thenReturn(BigDecimal.valueOf(2000));

            BigDecimal balance = accountService.getBalanceBetweenDates(1L, startDate, endDate);

            assertThat(balance).isEqualByComparingTo(BigDecimal.valueOf(3000));
        }

        @Test
        @DisplayName("Should throw exception when getting balance for non-existent account")
        void getBalance_AccountNotFound_ThrowsException() {
            when(journalEntryLineRepository.sumDebitByAccountId(99L)).thenReturn(BigDecimal.ZERO);
            when(journalEntryLineRepository.sumCreditByAccountId(99L)).thenReturn(BigDecimal.ZERO);
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getBalance(99L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Account not found");
        }
    }

    @Nested
    @DisplayName("Create Account If Not Exists")
    class CreateAccountIfNotExists {

        @Test
        @DisplayName("Should return existing account when code exists")
        void createAccountIfNotExists_CodeExists_ReturnsExisting() {
            when(accountRepository.findByCode("1000")).thenReturn(Optional.of(cashAccount));

            Account result = accountService.createAccountIfNotExists("1000", "Cash", AccountType.ASSET, "Cash account");

            assertThat(result).isEqualTo(cashAccount);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should create new account when code does not exist")
        void createAccountIfNotExists_CodeNotExists_CreatesNew() {
            Account newAccount = TestDataBuilder.createAccount(10L, "1500", "Inventory", AccountType.ASSET);
            when(accountRepository.findByCode("1500")).thenReturn(Optional.empty());
            when(accountRepository.save(any(Account.class))).thenReturn(newAccount);

            Account result = accountService.createAccountIfNotExists("1500", "Inventory", AccountType.ASSET, "Inventory account");

            assertThat(result.getCode()).isEqualTo("1500");
            verify(accountRepository).save(any(Account.class));
        }
    }
}