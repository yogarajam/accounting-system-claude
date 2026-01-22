package com.accounting.model;

public enum EntryStatus {
    DRAFT("Draft"),
    POSTED("Posted"),
    VOID("Void");

    private final String displayName;

    EntryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}