package com.accounting.service;

import com.accounting.dto.JournalEntryDTO;
import com.accounting.exception.AccountingException;
import com.accounting.model.*;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.JournalEntryRepository;
import com.accounting.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JournalService Unit Tests")
class JournalServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private JournalService journalService;

    private Account cashAccount;
    private Account revenueAccount;
    private JournalEntry draftEntry;
    private JournalEntry postedEntry;
    private User testUser;

    @BeforeEach
    void setUp() {
        cashAccount = TestDataBuilder.createCashAccount();
        revenueAccount = TestDataBuilder.createSalesRevenue();
        draftEntry = TestDataBuilder.createDraftEntry();
        postedEntry = TestDataBuilder.createPostedEntry();
        testUser = TestDataBuilder.createDefaultUser();
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should find all journal entries")
        void findAll_ReturnsAllEntries() {
            List<JournalEntry> entries = Arrays.asList(draftEntry, postedEntry);
            when(journalEntryRepository.findAll()).thenReturn(entries);

            List<JournalEntry> result = journalService.findAll();

            assertThat(result).hasSize(2);
            verify(journalEntryRepository).findAll();
        }

        @Test
        @DisplayName("Should find all journal entries with pagination")
        void findAll_WithPagination_ReturnsPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<JournalEntry> page = new PageImpl<>(Arrays.asList(draftEntry), pageable, 1);
            when(journalEntryRepository.findAll(pageable)).thenReturn(page);

            Page<JournalEntry> result = journalService.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should find journal entry by ID")
        void findById_WhenExists_ReturnsEntry() {
            when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(draftEntry));

            Optional<JournalEntry> result = journalService.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getEntryNumber()).isEqualTo("JE-202601-0001");
        }

        @Test
        @DisplayName("Should find journal entry by ID with lines")
        void findByIdWithLines_WhenExists_ReturnsEntryWithLines() {
            JournalEntry entryWithLines = TestDataBuilder.createBalancedJournalEntry(
                    cashAccount, revenueAccount, BigDecimal.valueOf(500));
            when(journalEntryRepository.findByIdWithLines(1L)).thenReturn(Optional.of(entryWithLines));

            Optional<JournalEntry> result = journalService.findByIdWithLines(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getLines()).hasSize(2);
        }

        @Test
        @DisplayName("Should find entries by status")
        void findByStatus_ReturnsMatchingEntries() {
            when(journalEntryRepository.findByStatus(EntryStatus.DRAFT))
                    .thenReturn(Arrays.asList(draftEntry));

            List<JournalEntry> result = journalService.findByStatus(EntryStatus.DRAFT);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(EntryStatus.DRAFT);
        }

        @Test
        @DisplayName("Should find posted entries between dates")
        void findPostedEntriesBetweenDates_ReturnsMatchingEntries() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();
            when(journalEntryRepository.findPostedEntriesBetweenDates(startDate, endDate))
                    .thenReturn(Arrays.asList(postedEntry));

            List<JournalEntry> result = journalService.findPostedEntriesBetweenDates(startDate, endDate);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should count entries by status")
        void countByStatus_ReturnsCorrectCount() {
            when(journalEntryRepository.countByStatus(EntryStatus.DRAFT)).thenReturn(5L);

            Long count = journalService.countByStatus(EntryStatus.DRAFT);

            assertThat(count).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("Create Entry Operations")
    class CreateEntryOperations {

        @Test
        @DisplayName("Should create valid journal entry")
        void createEntry_ValidDTO_CreatesSuccessfully() {
            JournalEntryDTO dto = TestDataBuilder.createJournalEntryDTO(1L, 4L, BigDecimal.valueOf(1000));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(accountRepository.findById(4L)).thenReturn(Optional.of(revenueAccount));
            when(journalEntryRepository.findMaxEntryNumberByPrefix(anyString())).thenReturn(null);
            when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> {
                JournalEntry entry = invocation.getArgument(0);
                entry.setId(1L);
                return entry;
            });

            JournalEntry result = journalService.createEntry(dto, testUser);

            assertThat(result.getStatus()).isEqualTo(EntryStatus.DRAFT);
            assertThat(result.getCreatedBy()).isEqualTo(testUser);
            assertThat(result.getLines()).hasSize(2);
            verify(journalEntryRepository).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("Should throw exception when account not found")
        void createEntry_AccountNotFound_ThrowsException() {
            JournalEntryDTO dto = TestDataBuilder.createJournalEntryDTO(99L, 4L, BigDecimal.valueOf(1000));
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> journalService.createEntry(dto, testUser))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        @DisplayName("Should generate entry number with correct format")
        void createEntry_GeneratesCorrectEntryNumber() {
            JournalEntryDTO dto = TestDataBuilder.createJournalEntryDTO(1L, 4L, BigDecimal.valueOf(1000));

            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(accountRepository.findById(4L)).thenReturn(Optional.of(revenueAccount));
            when(journalEntryRepository.findMaxEntryNumberByPrefix(anyString())).thenReturn(5);
            when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> i.getArgument(0));

            JournalEntry result = journalService.createEntry(dto, testUser);

            assertThat(result.getEntryNumber()).matches("JE-\\d{6}-0006");
        }
    }

    @Nested
    @DisplayName("Update Entry Operations")
    class UpdateEntryOperations {

        @Test
        @DisplayName("Should update draft entry successfully")
        void updateEntry_DraftEntry_UpdatesSuccessfully() {
            JournalEntryDTO dto = TestDataBuilder.createJournalEntryDTO(1L, 4L, BigDecimal.valueOf(2000));
            draftEntry.setLines(new ArrayList<>());

            when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(draftEntry));
            when(accountRepository.findById(1L)).thenReturn(Optional.of(cashAccount));
            when(accountRepository.findById(4L)).thenReturn(Optional.of(revenueAccount));
            when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> i.getArgument(0));

            JournalEntry result = journalService.updateEntry(1L, dto);

            assertThat(result.getLines()).hasSize(2);
            verify(journalEntryRepository).save(any(JournalEntry.class));
        }

        @Test
        @DisplayName("Should throw exception when updating posted entry")
        void updateEntry_PostedEntry_ThrowsException() {
            JournalEntryDTO dto = TestDataBuilder.createJournalEntryDTO(1L, 4L, BigDecimal.valueOf(2000));
            when(journalEntryRepository.findById(2L)).thenReturn(Optional.of(postedEntry));

            assertThatThrownBy(() -> journalService.updateEntry(2L, dto))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only draft entries can be modified");
        }

        @Test
        @DisplayName("Should throw exception when entry not found")
        void updateEntry_EntryNotFound_ThrowsException() {
            JournalEntryDTO dto = TestDataBuilder.createJournalEntryDTO(1L, 4L, BigDecimal.valueOf(2000));
            when(journalEntryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> journalService.updateEntry(99L, dto))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Journal entry not found");
        }
    }

    @Nested
    @DisplayName("Post Entry Operations")
    class PostEntryOperations {

        @Test
        @DisplayName("Should post draft entry successfully")
        void postEntry_DraftEntry_PostsSuccessfully() {
            JournalEntry balancedEntry = TestDataBuilder.createBalancedJournalEntry(
                    cashAccount, revenueAccount, BigDecimal.valueOf(1000));

            when(journalEntryRepository.findByIdWithLines(1L)).thenReturn(Optional.of(balancedEntry));
            when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> i.getArgument(0));

            JournalEntry result = journalService.postEntry(1L);

            assertThat(result.getStatus()).isEqualTo(EntryStatus.POSTED);
            assertThat(result.getPostedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when posting already posted entry")
        void postEntry_AlreadyPosted_ThrowsException() {
            when(journalEntryRepository.findByIdWithLines(2L)).thenReturn(Optional.of(postedEntry));

            assertThatThrownBy(() -> journalService.postEntry(2L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only draft entries can be posted");
        }

        @Test
        @DisplayName("Should throw exception when entry not found")
        void postEntry_EntryNotFound_ThrowsException() {
            when(journalEntryRepository.findByIdWithLines(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> journalService.postEntry(99L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Journal entry not found");
        }
    }

    @Nested
    @DisplayName("Void Entry Operations")
    class VoidEntryOperations {

        @Test
        @DisplayName("Should void posted entry successfully")
        void voidEntry_PostedEntry_VoidsSuccessfully() {
            when(journalEntryRepository.findById(2L)).thenReturn(Optional.of(postedEntry));
            when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> i.getArgument(0));

            JournalEntry result = journalService.voidEntry(2L);

            assertThat(result.getStatus()).isEqualTo(EntryStatus.VOID);
        }

        @Test
        @DisplayName("Should throw exception when voiding draft entry")
        void voidEntry_DraftEntry_ThrowsException() {
            when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(draftEntry));

            assertThatThrownBy(() -> journalService.voidEntry(1L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only posted entries can be voided");
        }

        @Test
        @DisplayName("Should throw exception when voiding already void entry")
        void voidEntry_AlreadyVoid_ThrowsException() {
            JournalEntry voidEntry = TestDataBuilder.createJournalEntry(3L, "JE-202601-0003", EntryStatus.VOID);
            when(journalEntryRepository.findById(3L)).thenReturn(Optional.of(voidEntry));

            assertThatThrownBy(() -> journalService.voidEntry(3L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Entry is already void");
        }
    }

    @Nested
    @DisplayName("Delete Entry Operations")
    class DeleteEntryOperations {

        @Test
        @DisplayName("Should delete draft entry successfully")
        void deleteEntry_DraftEntry_DeletesSuccessfully() {
            when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(draftEntry));
            doNothing().when(journalEntryRepository).delete(draftEntry);

            journalService.deleteEntry(1L);

            verify(journalEntryRepository).delete(draftEntry);
        }

        @Test
        @DisplayName("Should throw exception when deleting posted entry")
        void deleteEntry_PostedEntry_ThrowsException() {
            when(journalEntryRepository.findById(2L)).thenReturn(Optional.of(postedEntry));

            assertThatThrownBy(() -> journalService.deleteEntry(2L))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("Only draft entries can be deleted");
        }
    }

    @Nested
    @DisplayName("Entry Validation")
    class EntryValidation {

        @Test
        @DisplayName("Should validate entry with no lines")
        void validateEntry_NoLines_ThrowsException() {
            JournalEntry entry = TestDataBuilder.createDraftEntry();
            entry.setLines(new ArrayList<>());

            assertThatThrownBy(() -> journalService.validateEntry(entry))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("at least one line");
        }

        @Test
        @DisplayName("Should validate entry with only one line")
        void validateEntry_OnlyOneLine_ThrowsException() {
            JournalEntry entry = TestDataBuilder.createDraftEntry();
            entry.setLines(new ArrayList<>());
            JournalEntryLine line = TestDataBuilder.createJournalEntryLine(1L, cashAccount,
                    BigDecimal.valueOf(100), BigDecimal.ZERO);
            entry.getLines().add(line);

            assertThatThrownBy(() -> journalService.validateEntry(entry))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("at least two lines");
        }

        @Test
        @DisplayName("Should validate unbalanced entry")
        void validateEntry_Unbalanced_ThrowsException() {
            JournalEntry entry = TestDataBuilder.createDraftEntry();
            entry.setLines(new ArrayList<>());

            JournalEntryLine debitLine = TestDataBuilder.createJournalEntryLine(1L, cashAccount,
                    BigDecimal.valueOf(1000), BigDecimal.ZERO);
            JournalEntryLine creditLine = TestDataBuilder.createJournalEntryLine(2L, revenueAccount,
                    BigDecimal.ZERO, BigDecimal.valueOf(500));
            entry.getLines().add(debitLine);
            entry.getLines().add(creditLine);

            assertThatThrownBy(() -> journalService.validateEntry(entry))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("must balance");
        }

        @Test
        @DisplayName("Should validate entry with zero total")
        void validateEntry_ZeroTotal_ThrowsException() {
            JournalEntry entry = TestDataBuilder.createDraftEntry();
            entry.setLines(new ArrayList<>());

            JournalEntryLine debitLine = TestDataBuilder.createJournalEntryLine(1L, cashAccount,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            JournalEntryLine creditLine = TestDataBuilder.createJournalEntryLine(2L, revenueAccount,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            entry.getLines().add(debitLine);
            entry.getLines().add(creditLine);

            assertThatThrownBy(() -> journalService.validateEntry(entry))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("total cannot be zero");
        }

        @Test
        @DisplayName("Should validate line with both debit and credit")
        void validateEntry_LineBothDebitAndCredit_ThrowsException() {
            JournalEntry entry = TestDataBuilder.createDraftEntry();
            entry.setLines(new ArrayList<>());

            // Create two lines with both debit and credit to make a balanced entry
            // that triggers the "both debit and credit" validation
            JournalEntryLine badLine1 = TestDataBuilder.createJournalEntryLine(1L, cashAccount,
                    BigDecimal.valueOf(100), BigDecimal.valueOf(100));
            JournalEntryLine badLine2 = TestDataBuilder.createJournalEntryLine(2L, revenueAccount,
                    BigDecimal.valueOf(100), BigDecimal.valueOf(100));
            entry.getLines().add(badLine1);
            entry.getLines().add(badLine2);

            assertThatThrownBy(() -> journalService.validateEntry(entry))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("cannot have both debit and credit");
        }

        @Test
        @DisplayName("Should validate entry with inactive account")
        void validateEntry_InactiveAccount_ThrowsException() {
            Account inactiveAccount = TestDataBuilder.createAccount(10L, "9999", "Inactive", AccountType.ASSET);
            inactiveAccount.setIsActive(false);

            JournalEntry entry = TestDataBuilder.createDraftEntry();
            entry.setLines(new ArrayList<>());

            JournalEntryLine debitLine = TestDataBuilder.createJournalEntryLine(1L, inactiveAccount,
                    BigDecimal.valueOf(100), BigDecimal.ZERO);
            JournalEntryLine creditLine = TestDataBuilder.createJournalEntryLine(2L, revenueAccount,
                    BigDecimal.ZERO, BigDecimal.valueOf(100));
            entry.getLines().add(debitLine);
            entry.getLines().add(creditLine);

            assertThatThrownBy(() -> journalService.validateEntry(entry))
                    .isInstanceOf(AccountingException.class)
                    .hasMessageContaining("inactive account");
        }

        @Test
        @DisplayName("Should pass validation for valid balanced entry")
        void validateEntry_ValidEntry_PassesValidation() {
            JournalEntry entry = TestDataBuilder.createBalancedJournalEntry(
                    cashAccount, revenueAccount, BigDecimal.valueOf(1000));

            journalService.validateEntry(entry);
            // No exception means validation passed
        }
    }
}