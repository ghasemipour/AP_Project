package com.ap.project.entity.general;

import com.ap.project.Enums.CouponType;
import com.ap.project.dto.CouponDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private String couponCode;
    @Column(nullable = false)
    private CouponType type;
    @Column(nullable = false)
    private int value;
    @Column(nullable = false)
    private int minPrice;
    @Column(nullable = false)
    private int userCount;
    @Column(nullable = false)
    private String startDate;
    @Column(nullable = false)
    private String endDate;

    public Coupon(CouponType type, String couponCode, Integer minPrice, Integer value, Integer userCount, String startDate, String endDate) {
        this.type = type;
        this.couponCode = couponCode;
        this.minPrice = minPrice;
        this.value = value;
        this.userCount = userCount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Coupon() {

    }

    public CouponDto getCouponDto() {
        return new CouponDto(id, couponCode, type, value, minPrice, userCount, startDate, endDate);
    }
}
