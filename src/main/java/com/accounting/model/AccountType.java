package com.accounting.model;

public enum AccountType {
    ASSET("Asset"),
    LIABILITY("Liability"),
    EQUITY("Equity"),
    REVENUE("Revenue"),
    EXPENSE("Expense");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDebitNormal() {
        return this == ASSET || this == EXPENSE;
    }

    public boolean isCreditNormal() {
        return this == LIABILITY || this == EQUITY || this == REVENUE;
    }

    public boolean isBalanceSheetAccount() {
        return this == ASSET || this == LIABILITY || this == EQUITY;
    }

    public boolean isIncomeStatementAccount() {
        return this == REVENUE || this == EXPENSE;
    }
}
