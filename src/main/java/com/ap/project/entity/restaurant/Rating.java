package com.ap.project.entity.restaurant;


import com.ap.project.dao.OrderDao;
import com.ap.project.dao.UserDao;
import com.ap.project.dto.RatingDto;
import com.ap.project.entity.user.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    public Rating(RatingDto req) {
        rating = req.getRating();
        comment = req.getComment();
        order = OrderDao.getOrderFromId(req.getOrder_id());
        imageBase64 = req.getImageBase64();
//        this.user = (Customer) UserDao.getUserById(req.getUserId());
    }

}
