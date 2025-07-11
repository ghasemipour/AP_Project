package com.ap.project.entity.restaurant;


import com.ap.project.Enums.Status;
import com.ap.project.dao.RestaurantDao;
import com.ap.project.dto.OrderDto;
import com.ap.project.dto.OrderItemDto;
import com.ap.project.entity.user.Courier;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.User;
import com.sun.net.httpserver.HttpExchange;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;

    @Column(nullable = false)
    private String delivery_address;

    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Customer user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private Courier courier;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private Integer coupon_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;


    public Order(OrderDto orderDto, HttpExchange exchange, Customer user) throws IOException {
        delivery_address = orderDto.getDelivery_address();
        restaurant = RestaurantDao.getRestaurantById(orderDto.getVendor_id());
        coupon_id = orderDto.getCoupon_id();
        this.user = user;
        for (OrderItemDto orderItemDto : orderDto.getItems()) {
            items.add(orderItemDto.mapper(exchange, this));
        }
    }

    public Order() {

    }

    public OrderDto getOrderDto() {
        return new OrderDto(delivery_address, restaurant.getId(), coupon_id, items, status, id);
    }
}
