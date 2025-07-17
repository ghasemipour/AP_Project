package com.ap.project.Enums;

public enum ApprovalStatus {
    WAITING("Waiting"),
    APPROVED("approved"),
    REJECTED("rejected");


    private final String status;

    ApprovalStatus(String status) {
        this.status = status;
    }

    public static ApprovalStatus fromString(String status) {
        if (status == null) return null;
        for (ApprovalStatus s : ApprovalStatus.values()) {
            if (s.status.equalsIgnoreCase(status)) {
                return s;
            }
        }
        return null;
    }
}
