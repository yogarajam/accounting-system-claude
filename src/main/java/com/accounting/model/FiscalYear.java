package com.accounting.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_years")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiscalYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_closed")
    private Boolean isClosed = false;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !isClosed && !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public boolean containsDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}