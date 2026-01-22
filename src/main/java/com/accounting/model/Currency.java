package com.accounting.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "currencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 3)
    private String code;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "symbol", length = 5)
    private String symbol;

    @Column(name = "exchange_rate", precision = 15, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "is_base")
    private Boolean isBase = false;

    public Currency(String code, String name, String symbol) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.exchangeRate = BigDecimal.ONE;
        this.isBase = false;
    }
}