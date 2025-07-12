package com.ap.project.dto;


import com.ap.project.dao.FoodItemDao;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.restaurant.OrderItem;
import com.sun.net.httpserver.HttpExchange;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter
public class OrderItemDto {
    private Integer item_id;
    private Integer quantity;


    public OrderItem mapper(HttpExchange exchange, Order order) throws IOException {
        return new OrderItem(
                FoodItemDao.getFoodByID(this.item_id, exchange),
                this.quantity,
                order
        );
    }
}
