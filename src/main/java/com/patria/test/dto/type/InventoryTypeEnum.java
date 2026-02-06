package com.patria.test.dto.type;

public enum InventoryTypeEnum {
    T("TOP UP"),
    W("WITHDRAW");

    private final String description;

    InventoryTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
