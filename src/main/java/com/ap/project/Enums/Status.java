package com.ap.project.Enums;

public enum Status {
    SUBMITTED("submitted"),
    UNPAID_AND_CANCELLED("unpaid and cancelled"),
    WAITING_VENDOR("waiting vendor"),
    CANCELLED("cancelled"),
    FINDING_COURIER("finding courier"),
    ON_THE_WAY("on the way"),
    COMPLETED("completed"),
    ACCEPTED("accepted"),
    Courier_Accepted("courier accepted"),
    DELIVERED("delivered"),
    RECEIVED("received"),
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

