package com.accounting.repository;

import com.accounting.model.EntryStatus;
import com.accounting.model.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    Optional<JournalEntry> findByEntryNumber(String entryNumber);
    boolean existsByEntryNumber(String entryNumber);

    List<JournalEntry> findByStatus(EntryStatus status);
    List<JournalEntry> findByEntryDateBetween(LocalDate startDate, LocalDate endDate);
    List<JournalEntry> findByStatusAndEntryDateBetween(EntryStatus status, LocalDate startDate, LocalDate endDate);

    Page<JournalEntry> findByStatus(EntryStatus status, Pageable pageable);

    @Query("SELECT j FROM JournalEntry j WHERE j.status = 'POSTED' ORDER BY j.entryDate DESC, j.entryNumber DESC")
    List<JournalEntry> findAllPosted();

    @Query("SELECT j FROM JournalEntry j WHERE j.status = 'POSTED' AND j.entryDate BETWEEN :startDate AND :endDate ORDER BY j.entryDate, j.entryNumber")
    List<JournalEntry> findPostedEntriesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT MAX(CAST(SUBSTRING(j.entryNumber, LENGTH(:prefix) + 2) AS int)) FROM JournalEntry j WHERE j.entryNumber LIKE CONCAT(:prefix, '-%')")
    Integer findMaxEntryNumberByPrefix(@Param("prefix") String prefix);

    @Query("SELECT j FROM JournalEntry j LEFT JOIN FETCH j.lines WHERE j.id = :id")
    Optional<JournalEntry> findByIdWithLines(@Param("id") Long id);

    @Query("SELECT COUNT(j) FROM JournalEntry j WHERE j.status = :status")
    Long countByStatus(@Param("status") EntryStatus status);

    @Query("SELECT DISTINCT j FROM JournalEntry j LEFT JOIN FETCH j.lines")
    List<JournalEntry> findAllWithLines();

    @Query(value = "SELECT DISTINCT j FROM JournalEntry j LEFT JOIN FETCH j.lines",
           countQuery = "SELECT COUNT(j) FROM JournalEntry j")
    Page<JournalEntry> findAllWithLines(Pageable pageable);
}