package com.ap.project.entity.restaurant;


import com.ap.project.Enums.Status;
import com.ap.project.dao.RestaurantDao;
import com.ap.project.dto.OrderDto;
import com.ap.project.dto.OrderItemDto;
import com.ap.project.entity.general.Transaction;
import com.ap.project.entity.user.Courier;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.User;
import com.sun.net.httpserver.HttpExchange;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;

    @Column(nullable = false)
    private String delivery_address;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Customer user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private Courier courier;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    private Integer coupon_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Rating rating;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Transaction transaction;
    // TODO: CHECK HOW TO CALCULATE TAX FEE AND COURIER FEE
    private Integer raw_price;
    private Integer tax_fee = 0;
    private Integer courier_fee = 0;
    private Integer additional_fee = 0;
    private Integer pay_price;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime created_at;

    @UpdateTimestamp
    private LocalDateTime updated_at;

    public Order(OrderDto orderDto, HttpExchange exchange, Customer user, Status status) throws IOException {
        delivery_address = orderDto.getDelivery_address();
        restaurant = RestaurantDao.getRestaurantById(orderDto.getVendor_id());
        coupon_id = orderDto.getCoupon_id();
        this.user = user;
        for (OrderItemDto orderItemDto : orderDto.getItems()) {
            items.add(orderItemDto.mapper(exchange, this));
        }
        raw_price = calculateRawPrice();
        pay_price = calculatePayPrice();
        this.status = status;
    }

    public Order() {

    }

    public OrderDto getOrderDto() {
        return new OrderDto(delivery_address, restaurant.getId(), coupon_id, items, status, id, user.getUserId(), created_at, updated_at, raw_price, tax_fee, additional_fee, courier_fee, pay_price, courier);
    }

    public void addRating(Rating rating) {
        this.rating = rating;
        rating.setOrder(this);
    }

    public void removeRating(Rating rating) {
        this.rating = null;
        rating.setOrder(null);
    }

    private Integer calculateRawPrice() {
        int rawPrice = 0;
        for (OrderItem orderItem: items) {
            rawPrice += (orderItem.getFood().getPrice() * orderItem.getQuantity());
        }
        return rawPrice;
    }

    private Integer calculatePayPrice() {
        return raw_price + tax_fee + courier_fee + additional_fee;
    }
}
