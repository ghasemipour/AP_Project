package com.ap.project.Enums;

public enum TransactionStatus {
    SUCCESS("success"),
    FAILED("failed");


    private final String status;

    TransactionStatus(String status) {
        this.status = status;
    }
}
