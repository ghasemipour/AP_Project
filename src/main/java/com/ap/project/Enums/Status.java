package com.ap.project.Enums;

public enum Status {
    SUBMITTED("submitted"), //buyer
    PAYMENT_FAILED("payment failed"), //buyer
    WAITING_VENDOR("waiting vendor"), //buyer (payment completed)
    CANCELLED("cancelled"),
    FINDING_COURIER("finding courier"), //for seller
    ON_THE_WAY("on the way"),
    ACCEPTED("accepted"), //for seller (update supplies) DONE
    Courier_Accepted("courier accepted"),
    DELIVERED("delivered"),
    RECEIVED("received"), // courier receiving item
    REJECTED("rejected"); //for seller (refund)

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

