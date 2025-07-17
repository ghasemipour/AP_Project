package com.ap.project.dto;

import com.ap.project.Enums.Status;
import com.ap.project.entity.restaurant.OrderItem;
import com.ap.project.entity.user.Courier;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class OrderDto {
    private String delivery_address;
    private Integer vendor_id;
    private Integer coupon_id;
    private List<OrderItemDto> items = new ArrayList<>();
    private Status status;
    private Integer order_id;
    private Integer user_id;
    private Integer courier_id;

    private Integer raw_price;
    private Integer tax_fee;
    private Integer courier_fee;
    private Integer additional_fee;
    private Integer pay_price;

    private String created_at;
    private String updated_at;

    public OrderDto(String delivery_address, Integer vendor_id, Integer coupon_id, List<OrderItem> items, Status status, Integer order_id, Integer user_id, LocalDateTime created_at, LocalDateTime updated_at, Integer raw_price, Integer tax_fee, Integer additional_fee, Integer courier_fee, Integer pay_price, Courier courier) {
        this.delivery_address = delivery_address;
        this.vendor_id = vendor_id;
        this.coupon_id = coupon_id;
        for (OrderItem orderItem : items) {
            this.items.add(orderItem.getDto());
        }
        this.status = status;
        this.order_id = order_id;
        this.user_id = user_id;
        if (courier != null)
            this.courier_id = courier.getUserId();

        this.created_at = created_at.toString();
        this.updated_at = updated_at.toString();

        this.raw_price = raw_price;
        this.additional_fee = additional_fee;
        this.courier_fee = courier_fee;
        this.tax_fee = tax_fee;
        this.pay_price = pay_price;
    }
}
