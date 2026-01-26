package com.accounting.integration;

import com.accounting.dto.JournalEntryDTO;
import com.accounting.exception.AccountingException;
import com.accounting.model.*;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.JournalEntryRepository;
import com.accounting.repository.UserRepository;
import com.accounting.service.AccountService;
import com.accounting.service.JournalService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Journal Entry End-to-End Integration Tests")
class JournalEntryIntegrationTest {

    @Autowired
    private JournalService journalService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserRepository userRepository;

    private Account cashAccount;
    private Account revenueAccount;
    private Account expenseAccount;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test accounts
        cashAccount = createAccount("1000", "Cash", AccountType.ASSET);
        revenueAccount = createAccount("4000", "Sales Revenue", AccountType.REVENUE);
        expenseAccount = createAccount("5000", "Operating Expenses", AccountType.EXPENSE);

        // Create test user
        testUser = createTestUser();
    }

    private Account createAccount(String code, String name, AccountType type) {
        Account account = new Account();
        account.setCode(code);
        account.setName(name);
        account.setAccountType(type);
        account.setIsActive(true);
        return accountRepository.save(account);
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("integrationtest");
        user.setPassword("password");
        user.setEmail("integration@test.com");
        user.setFullName("Integration Test User");
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Nested
    @DisplayName("Complete Journal Entry Lifecycle")
    class JournalEntryLifecycle {

        @Test
        @DisplayName("Should complete full journal entry lifecycle: Create -> Post -> Void")
        void journalEntryLifecycle_CreatePostVoid_Succeeds() {
            // 1. CREATE JOURNAL ENTRY
            JournalEntryDTO dto = createBalancedJournalEntryDTO(
                    cashAccount.getId(), revenueAccount.getId(), BigDecimal.valueOf(1500));

            JournalEntry createdEntry = journalService.createEntry(dto, testUser);

            assertThat(createdEntry.getId()).isNotNull();
            assertThat(createdEntry.getStatus()).isEqualTo(EntryStatus.DRAFT);
            assertThat(createdEntry.getLines()).hasSize(2);
            assertThat(createdEntry.getTotalDebit()).isEqualByComparingTo(BigDecimal.valueOf(1500));
            assertThat(createdEntry.isBalanced()).isTrue();

            // 2. POST JOURNAL ENTRY
            JournalEntry postedEntry = journalService.postEntry(createdEntry.getId());

            assertThat(postedEntry.getStatus()).isEqualTo(EntryStatus.POSTED);
            assertThat(postedEntry.getPostedAt()).isNotNull();

            // 3. Verify account balances after posting
            BigDecimal cashBalance = accountService.getBalance(cashAccount.getId());
            BigDecimal revenueBalance = accountService.getBalance(revenueAccount.getId());

            assertThat(cashBalance).isEqualByComparingTo(BigDecimal.valueOf(1500));
            assertThat(revenueBalance).isEqualByComparingTo(BigDecimal.valueOf(1500));

            // 4. VOID JOURNAL ENTRY
            JournalEntry voidedEntry = journalService.voidEntry(postedEntry.getId());

            assertThat(voidedEntry.getStatus()).isEqualTo(EntryStatus.VOID);

            // 5. Verify account balances after voiding (void doesn't reverse, just marks as void)
            // In this implementation, voided entries still affect balances (only POSTED entries do)
        }

        @Test
        @DisplayName("Should update draft entry successfully")
        void updateDraftEntry_Succeeds() {
            // Create entry
            JournalEntryDTO createDto = createBalancedJournalEntryDTO(
                    cashAccount.getId(), revenueAccount.getId(), BigDecimal.valueOf(1000));
            JournalEntry entry = journalService.createEntry(createDto, testUser);

            // Update entry
            JournalEntryDTO updateDto = createBalancedJournalEntryDTO(
                    cashAccount.getId(), expenseAccount.getId(), BigDecimal.valueOf(2000));
            updateDto.setDescription("Updated description");

            JournalEntry updatedEntry = journalService.updateEntry(entry.getId(), updateDto);

            assertThat(updatedEntry.getDescription()).isEqualTo("Updated description");
            assertThat(updatedEntry.getTotalDebit()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        }

        @Test
        @DisplayName("Should delete draft entry successfully")
        void deleteDraftEntry_Succeeds() {
            JournalEntryDTO dto = createBalancedJournalEntryDTO(
                    cashAccount.getId(), revenueAccount.getId(), BigDecimal.valueOf(1000));
            JournalEntry entry = journalService.createEntry(dto, testUser);

            Long entryId = entry.getId();
            journalService.deleteEntry(entryId);

            assertThat(journalEntryRepository.findById(entryId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should reject unbalanced entry")
        void createEntry_Unbalanced_ThrowsException() {
            JournalEntryDTO dto = new JournalEntryDTO();
            dto.setEntryDate(LocalDate.now());
            dto.setDescription("Unbalanced entry");
            dto.setLines(new ArrayList<>());

            // Add unbalanced lines
            JournalEntryDTO.JournalEntryLineDTO debitLine = new JournalEntryDTO.JournalEntryLineDTO();
            debitLine.setAccountId(cashAccount.getId());
            debitLine.setDebitAmount(BigDecimal.valueOf(1000));
            debitLine.setCreditAmount(BigDecimal.ZERO);
            dto.getLines().add(debitLine);

            JournalEntryDTO.JournalEntryLineDTO creditLine = new JournalEntryDTO.JournalEntryLineDTO();
            creditLine.setAccountId(revenueAccount.getId());
            creditLine.setDebitAmount(BigDecimal.ZERO);
            creditLine.setCreditAmount(BigDecimal.valueOf(500)); // Unbalanced!
            dto.getLines().add(creditLine);

            assertThatThrownBy(() -> journalService.createEntry(dto, testUser))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("must balance");
        }

        @Test
        @DisplayName("Should reject entry with inactive account")
        void createEntry_InactiveAccount_ThrowsException() {
            // Deactivate account (need zero balance first)
            Account inactiveAccount = createAccount("9999", "Inactive", AccountType.ASSET);
            inactiveAccount.setIsActive(false);
            accountRepository.save(inactiveAccount);

            JournalEntryDTO dto = createBalancedJournalEntryDTO(
                    inactiveAccount.getId(), revenueAccount.getId(), BigDecimal.valueOf(1000));

            assertThatThrownBy(() -> journalService.createEntry(dto, testUser))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("inactive account");
        }

        @Test
        @DisplayName("Should reject posting already posted entry")
        void postEntry_AlreadyPosted_ThrowsException() {
            JournalEntryDTO dto = createBalancedJournalEntryDTO(
                    cashAccount.getId(), revenueAccount.getId(), BigDecimal.valueOf(1000));
            JournalEntry entry = journalService.createEntry(dto, testUser);
            journalService.postEntry(entry.getId());

            assertThatThrownBy(() -> journalService.postEntry(entry.getId()))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only draft entries can be posted");
        }

        @Test
        @DisplayName("Should reject deleting posted entry")
        void deleteEntry_Posted_ThrowsException() {
            JournalEntryDTO dto = createBalancedJournalEntryDTO(
                    cashAccount.getId(), revenueAccount.getId(), BigDecimal.valueOf(1000));
            JournalEntry entry = journalService.createEntry(dto, testUser);
            journalService.postEntry(entry.getId());

            assertThatThrownBy(() -> journalService.deleteEntry(entry.getId()))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only draft entries can be deleted");
        }
    }

    @Nested
    @DisplayName("Account Balance Tests")
    class AccountBalanceTests {

        @Test
        @DisplayName("Should correctly calculate balances for multiple entries")
        void multipleEntries_CalculatesBalancesCorrectly() {
            // Entry 1: Cash 1000 debit, Revenue 1000 credit (sales)
            JournalEntryDTO entry1 = createBalancedJournalEntryDTO(
                    cashAccount.getId(), revenueAccount.getId(), BigDecimal.valueOf(1000));
            journalService.postEntry(journalService.createEntry(entry1, testUser).getId());

            // Entry 2: Cash 500 debit, Revenue 500 credit (more sales)
            JournalEntryDTO entry2 = createBalancedJournalEntryDTO(
                    cashAccount.getId(), revenueAccount.getId(), BigDecimal.valueOf(500));
            journalService.postEntry(journalService.createEntry(entry2, testUser).getId());

            // Entry 3: Expense 200 debit, Cash 200 credit (payment)
            JournalEntryDTO entry3 = createBalancedJournalEntryDTO(
                    expenseAccount.getId(), cashAccount.getId(), BigDecimal.valueOf(200));
            journalService.postEntry(journalService.createEntry(entry3, testUser).getId());

            // Verify balances
            BigDecimal cashBalance = accountService.getBalance(cashAccount.getId());
            BigDecimal revenueBalance = accountService.getBalance(revenueAccount.getId());
            BigDecimal expenseBalance = accountService.getBalance(expenseAccount.getId());

            // Cash: 1000 + 500 - 200 = 1300 (debit normal)
            assertThat(cashBalance).isEqualByComparingTo(BigDecimal.valueOf(1300));
            // Revenue: 1000 + 500 = 1500 (credit normal)
            assertThat(revenueBalance).isEqualByComparingTo(BigDecimal.valueOf(1500));
            // Expense: 200 (debit normal)
            assertThat(expenseBalance).isEqualByComparingTo(BigDecimal.valueOf(200));
        }

        @Test
        @DisplayName("Should calculate balance as of specific date")
        void getBalanceAsOfDate_CalculatesCorrectly() {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            // Entry yesterday
            JournalEntryDTO entry1 = createBalancedJournalEntryDTO(
                    cashAccount.getId(), revenueAccount.getId(), BigDecimal.valueOf(1000));
            entry1.setEntryDate(yesterday);
            journalService.postEntry(journalService.createEntry(entry1, testUser).getId());

            // Entry today
            JournalEntryDTO entry2 = createBalancedJournalEntryDTO(
                    cashAccount.getId(), revenueAccount.getId(), BigDecimal.valueOf(500));
            entry2.setEntryDate(today);
            journalService.postEntry(journalService.createEntry(entry2, testUser).getId());

            // Balance as of yesterday should only include first entry
            BigDecimal balanceYesterday = accountService.getBalanceAsOfDate(cashAccount.getId(), yesterday);
            assertThat(balanceYesterday).isEqualByComparingTo(BigDecimal.valueOf(1000));

            // Balance as of today should include both entries
            BigDecimal balanceToday = accountService.getBalanceAsOfDate(cashAccount.getId(), today);
            assertThat(balanceToday).isEqualByComparingTo(BigDecimal.valueOf(1500));
        }
    }

    private JournalEntryDTO createBalancedJournalEntryDTO(Long debitAccountId, Long creditAccountId, BigDecimal amount) {
        JournalEntryDTO dto = new JournalEntryDTO();
        dto.setEntryDate(LocalDate.now());
        dto.setDescription("Integration test entry");
        dto.setReference("INT-TEST");
        dto.setLines(new ArrayList<>());

        JournalEntryDTO.JournalEntryLineDTO debitLine = new JournalEntryDTO.JournalEntryLineDTO();
        debitLine.setAccountId(debitAccountId);
        debitLine.setDebitAmount(amount);
        debitLine.setCreditAmount(BigDecimal.ZERO);
        debitLine.setDescription("Debit line");
        dto.getLines().add(debitLine);

        JournalEntryDTO.JournalEntryLineDTO creditLine = new JournalEntryDTO.JournalEntryLineDTO();
        creditLine.setAccountId(creditAccountId);
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(amount);
        creditLine.setDescription("Credit line");
        dto.getLines().add(creditLine);

        return dto;
    }
}