package com.accounting.repository;

import com.accounting.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByCode(String code);
    boolean existsByCode(String code);

    List<Vendor> findByNameContainingIgnoreCase(String name);

    @Query("SELECT v FROM Vendor v WHERE LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Vendor> searchByNameOrCode(@Param("search") String search);
}