package com.ap.project.entity.restaurant;


import com.ap.project.dao.OrderDao;
import com.ap.project.dao.UserDao;
import com.ap.project.dto.RatingDto;
import com.ap.project.entity.user.Customer;
import com.sun.net.httpserver.HttpExchange;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected int id;

    @Column (nullable = false)
    @Min(1)
    @Max(5)
    private Integer rating;
    @Column (nullable = false)
    private String comment;
    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer user;

    @ElementCollection
    private List<String> imageBase64 = new ArrayList<>();

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime created_at;

    @UpdateTimestamp
    private LocalDateTime updated_at;

    public Rating(RatingDto req, HttpExchange exchange) {
        rating = req.getRating();
        comment = req.getComment();
        order = OrderDao.getOrderFromId(req.getOrder_id(), exchange);
        imageBase64 = req.getImageBase64();
        this.user = (Customer) UserDao.getUserById(req.getUserId());
    }

    public Rating() {

    }

    public RatingDto getRatingDto() {
        return new RatingDto(order.id, rating, comment, imageBase64, user.getUserId(), id, created_at, updated_at);
    }

}
