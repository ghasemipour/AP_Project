package com.ap.project.Enums;

public enum TransactionStatus {
    SUCCESS("success"),
    FAILED("failed");


    private final String status;

    TransactionStatus(String status) {
        this.status = status;
    }

    public static TransactionStatus fromString(String status) {
        if (status == null) return null;
        for (TransactionStatus s : TransactionStatus.values()) {
            if (s.status.equalsIgnoreCase(status)) {
                return s;
            }
        }
        return null;
    }
}
