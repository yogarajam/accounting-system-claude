package com.accounting.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Account Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String accountType;
    private Long parentId;
    private Long currencyId;
    private Boolean isActive;
    private BigDecimal balance;
    private BigDecimal openingBalance;

    public static AccountDTO createAsset(String code, String name) {
        return AccountDTO.builder()
                .code(code)
                .name(name)
                .accountType("ASSET")
                .isActive(true)
                .build();
    }

    public static AccountDTO createLiability(String code, String name) {
        return AccountDTO.builder()
                .code(code)
                .name(name)
                .accountType("LIABILITY")
                .isActive(true)
                .build();
    }

    public static AccountDTO createEquity(String code, String name) {
        return AccountDTO.builder()
                .code(code)
                .name(name)
                .accountType("EQUITY")
                .isActive(true)
                .build();
    }

    public static AccountDTO createRevenue(String code, String name) {
        return AccountDTO.builder()
                .code(code)
                .name(name)
                .accountType("REVENUE")
                .isActive(true)
                .build();
    }

    public static AccountDTO createExpense(String code, String name) {
        return AccountDTO.builder()
                .code(code)
                .name(name)
                .accountType("EXPENSE")
                .isActive(true)
                .build();
    }
}