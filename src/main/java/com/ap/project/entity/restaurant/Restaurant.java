package com.ap.project.entity.restaurant;

import com.ap.project.dto.RestaurantDto;
import com.ap.project.entity.user.Seller;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Restaurant {

    @Id
    private String id;

    @Embedded
    private RestaurantDto restaurantInfo;

    @ManyToOne
    private Seller owener;

    public Restaurant(RestaurantDto restaurantInfo, Seller owener) {
        this.restaurantInfo = restaurantInfo;
        this.owener = owener;
        id = "R-" + UUID.randomUUID().toString();
    }

}
