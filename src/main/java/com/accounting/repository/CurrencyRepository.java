package com.accounting.repository;

import com.accounting.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT c FROM Currency c WHERE c.isBase = true")
    Optional<Currency> findBaseCurrency();
}