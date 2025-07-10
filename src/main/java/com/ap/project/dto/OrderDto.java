package com.ap.project.dto;

import com.ap.project.entity.restaurant.OrderItem;

import java.util.List;

public class OrderDto {
    private String delivery_address;
    private Integer vendor_id;
    private Integer coupon_id;
    private List<OrderItemDto> items;
}
