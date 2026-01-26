package com.accounting.repository;

import com.accounting.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("JournalEntryRepository Integration Tests")
class JournalEntryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    private Account cashAccount;
    private Account revenueAccount;
    private JournalEntry draftEntry;
    private JournalEntry postedEntry;

    @BeforeEach
    void setUp() {
        // Create accounts
        cashAccount = createAndSaveAccount("1000", "Cash", AccountType.ASSET);
        revenueAccount = createAndSaveAccount("4000", "Revenue", AccountType.REVENUE);

        // Create journal entries
        draftEntry = createJournalEntry("JE-202601-0001", EntryStatus.DRAFT, LocalDate.now());
        postedEntry = createJournalEntry("JE-202601-0002", EntryStatus.POSTED, LocalDate.now().minusDays(5));
        postedEntry.setPostedAt(LocalDateTime.now());

        addLinesToEntry(draftEntry, BigDecimal.valueOf(1000));
        addLinesToEntry(postedEntry, BigDecimal.valueOf(2000));

        entityManager.persist(draftEntry);
        entityManager.persist(postedEntry);
        entityManager.flush();
        entityManager.clear();
    }

    private Account createAndSaveAccount(String code, String name, AccountType type) {
        Account account = new Account();
        account.setCode(code);
        account.setName(name);
        account.setAccountType(type);
        account.setIsActive(true);
        return entityManager.persist(account);
    }

    private JournalEntry createJournalEntry(String entryNumber, EntryStatus status, LocalDate entryDate) {
        JournalEntry entry = new JournalEntry();
        entry.setEntryNumber(entryNumber);
        entry.setEntryDate(entryDate);
        entry.setDescription("Test entry");
        entry.setStatus(status);
        return entry;
    }

    private void addLinesToEntry(JournalEntry entry, BigDecimal amount) {
        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setAccount(cashAccount);
        debitLine.setDebitAmount(amount);
        debitLine.setCreditAmount(BigDecimal.ZERO);
        debitLine.setDescription("Debit");
        entry.addLine(debitLine);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setAccount(revenueAccount);
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(amount);
        creditLine.setDescription("Credit");
        entry.addLine(creditLine);
    }

    @Nested
    @DisplayName("Find By Entry Number")
    class FindByEntryNumber {

        @Test
        @DisplayName("Should find entry by entry number")
        void findByEntryNumber_WhenExists_ReturnsEntry() {
            Optional<JournalEntry> result = journalEntryRepository.findByEntryNumber("JE-202601-0001");

            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(EntryStatus.DRAFT);
        }

        @Test
        @DisplayName("Should return empty when entry number does not exist")
        void findByEntryNumber_WhenNotExists_ReturnsEmpty() {
            Optional<JournalEntry> result = journalEntryRepository.findByEntryNumber("JE-INVALID");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find By Status")
    class FindByStatus {

        @Test
        @DisplayName("Should find entries by status")
        void findByStatus_ReturnsMatchingEntries() {
            List<JournalEntry> result = journalEntryRepository.findByStatus(EntryStatus.DRAFT);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEntryNumber()).isEqualTo("JE-202601-0001");
        }

        @Test
        @DisplayName("Should return paginated results by status")
        void findByStatus_Pageable_ReturnsPaginatedResults() {
            Page<JournalEntry> result = journalEntryRepository.findByStatus(
                    EntryStatus.POSTED, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Find By Date Range")
    class FindByDateRange {

        @Test
        @DisplayName("Should find entries between dates")
        void findByEntryDateBetween_ReturnsMatchingEntries() {
            LocalDate startDate = LocalDate.now().minusDays(10);
            LocalDate endDate = LocalDate.now().plusDays(1);

            List<JournalEntry> result = journalEntryRepository.findByEntryDateBetween(startDate, endDate);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should find posted entries between dates")
        void findPostedEntriesBetweenDates_ReturnsOnlyPostedEntries() {
            LocalDate startDate = LocalDate.now().minusDays(10);
            LocalDate endDate = LocalDate.now();

            List<JournalEntry> result = journalEntryRepository.findPostedEntriesBetweenDates(startDate, endDate);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(EntryStatus.POSTED);
        }
    }

    @Nested
    @DisplayName("Count By Status")
    class CountByStatus {

        @Test
        @DisplayName("Should count entries by status")
        void countByStatus_ReturnsCorrectCount() {
            Long count = journalEntryRepository.countByStatus(EntryStatus.DRAFT);

            assertThat(count).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Find With Lines (Fetch Join)")
    class FindWithLines {

        @Test
        @DisplayName("Should find entry by ID with lines eagerly loaded")
        void findByIdWithLines_ReturnsEntryWithLines() {
            Optional<JournalEntry> result = journalEntryRepository.findByIdWithLines(draftEntry.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getLines()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Find Max Entry Number By Prefix")
    class FindMaxEntryNumberByPrefix {

        @Test
        @DisplayName("Should find max entry number by prefix")
        void findMaxEntryNumberByPrefix_ReturnsMaxNumber() {
            Integer result = journalEntryRepository.findMaxEntryNumberByPrefix("JE-202601");

            assertThat(result).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return null when no entries with prefix exist")
        void findMaxEntryNumberByPrefix_WhenNoMatch_ReturnsNull() {
            Integer result = journalEntryRepository.findMaxEntryNumberByPrefix("JE-202612");

            assertThat(result).isNull();
        }
    }
}