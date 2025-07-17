package com.ap.project.dto;

import com.ap.project.Enums.CouponType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponDto {
    private int id;
    private String couponCode;
    private CouponType type;
    private Integer value;
    private Integer minPrice;
    private Integer userCount;
    private String startDate;
    private String endDate;

    public CouponDto(int id, String couponCode, CouponType type, int value, int minPrice, int userCount, String startDate, String endDate) {
        this.id = id;
        this.couponCode = couponCode;
        this.type = type;
        this.value = value;
        this.minPrice = minPrice;
        this.userCount = userCount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public CouponDto() {}
}
