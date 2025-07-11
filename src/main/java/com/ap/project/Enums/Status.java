package com.ap.project.Enums;

public enum Status {
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    SERVED("served");

    private final String orderStatus;

    Status(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public static Status fromString(String status) {
        if (status == null) return null;
        for (Status s : Status.values()) {
            if (s.orderStatus.equalsIgnoreCase(status)) {
                return s;
            }
        }
        return null;
    }
}

