package com.accounting.repository;

import com.accounting.model.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FiscalYearRepository extends JpaRepository<FiscalYear, Long> {

    @Query("SELECT fy FROM FiscalYear fy WHERE fy.isClosed = false AND :date BETWEEN fy.startDate AND fy.endDate")
    Optional<FiscalYear> findActiveByDate(@Param("date") LocalDate date);

    @Query("SELECT fy FROM FiscalYear fy WHERE fy.isClosed = false ORDER BY fy.startDate DESC")
    List<FiscalYear> findAllOpen();

    @Query("SELECT fy FROM FiscalYear fy WHERE :date BETWEEN fy.startDate AND fy.endDate")
    Optional<FiscalYear> findByDate(@Param("date") LocalDate date);

    @Query("SELECT fy FROM FiscalYear fy ORDER BY fy.startDate DESC")
    List<FiscalYear> findAllOrderByStartDateDesc();

    Optional<FiscalYear> findByName(String name);
}