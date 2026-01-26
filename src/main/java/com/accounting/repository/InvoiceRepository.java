package com.accounting.repository;

import com.accounting.model.Invoice;
import com.accounting.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    boolean existsByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByStatus(InvoiceStatus status);
    List<Invoice> findByCustomerId(Long customerId);
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'SENT' AND i.dueDate < :today")
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);

    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT MAX(CAST(SUBSTRING(i.invoiceNumber, LENGTH(:prefix) + 2) AS int)) FROM Invoice i WHERE i.invoiceNumber LIKE CONCAT(:prefix, '-%')")
    Integer findMaxInvoiceNumberByPrefix(@Param("prefix") String prefix);

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.id = :id")
    Optional<Invoice> findByIdWithItems(@Param("id") Long id);
}