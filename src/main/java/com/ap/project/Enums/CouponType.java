package com.ap.project.Enums;

public enum CouponType {
    FIXED("fixed"),
    PERCENT("percent");

    private final String couponType;

    CouponType(String couponType) {
        this.couponType = couponType;
    }

    public String getCouponType() {
        return couponType;
    }

    public static CouponType fromString(String couponType) {
        if (couponType == null) return null;
        for (CouponType s : CouponType.values()) {
            if (s.couponType.equalsIgnoreCase(couponType)) {
                return s;
            }
        }
        return null;
    }
}