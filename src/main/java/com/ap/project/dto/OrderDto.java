package com.ap.project.dto;

import com.ap.project.Enums.Status;
import com.ap.project.entity.restaurant.OrderItem;
import lombok.Getter;
import lombok.Setter;

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

    public OrderDto(String delivery_address, Integer vendor_id, Integer coupon_id, List<OrderItem> items, Status status, Integer order_id) {
        this.delivery_address = delivery_address;
        this.vendor_id = vendor_id;
        this.coupon_id = coupon_id;
        for (OrderItem orderItem : items) {
            this.items.add(orderItem.getDto());
        }
        this.status = status;
        this.order_id = order_id;
    }
}
