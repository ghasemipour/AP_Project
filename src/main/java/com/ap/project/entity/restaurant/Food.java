package com.ap.project.entity.restaurant;

import com.ap.project.dao.RestaurantDao;
import com.ap.project.dto.FoodDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Entity
@Getter
@Setter
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int foodId;

    @Column(nullable = false)
    private String name;
    private String imageBase64;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private Integer price;
    @Column(nullable = false)
    private Integer supply;
    @Column
    @ElementCollection
    private List<String> keywords = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToMany(mappedBy = "foodItems")
    private List<Menu> menus = new ArrayList<>();

    public Food (FoodDto foodDto, Restaurant restaurant) {
        name = foodDto.getName();
        imageBase64 = foodDto.getImageBase64();
        description = foodDto.getDescription();
        price = foodDto.getPrice();
        supply = foodDto.getSupply();
        keywords = foodDto.getKeywords();
        this.restaurant = restaurant;
    }

    public Food() {}
}
