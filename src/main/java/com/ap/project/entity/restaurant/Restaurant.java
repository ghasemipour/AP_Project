package com.ap.project.entity.restaurant;

import com.ap.project.dto.RestaurantDto;
import com.ap.project.dto.WorkingHourDto;
import com.ap.project.entity.user.Seller;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private boolean IsConfirmed = true;
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
    private int tax_fee = 0;
    private int additional_fee = 0;
    @Embedded
    private WorkingHourDto working_hour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Seller owner;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Food> foodItems = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    public Restaurant(RestaurantDto restaurantInfo, Seller owner) {
        this.name = restaurantInfo.getName();
        this.address = restaurantInfo.getAddress();
        this.phone = restaurantInfo.getPhone();
        this.logoBase64 = restaurantInfo.getLogoBase64();
        if(restaurantInfo.getTax_fee() != null)
            this.tax_fee = restaurantInfo.getTax_fee();
        if(restaurantInfo.getAdditional_fee() != null)
            this.additional_fee = restaurantInfo.getAdditional_fee();
        this.working_hour = restaurantInfo.getWorking_hour();
        this.owner = owner;
    }

    public Restaurant() {}

    public RestaurantDto GetDto() {
        RestaurantDto restaurantDto = new RestaurantDto(id, name, address, phone, logoBase64, tax_fee, additional_fee, working_hour);
        return restaurantDto;
    }

    public void addFood(Food food) {
        foodItems.add(food);
        food.setRestaurant(this);
    }

    public void removeFood(Food food) {
        foodItems.remove(food);
        food.setRestaurant(null);
    }

    public void addMenu(Menu menu) {
        menus.add(menu);
        menu.setRestaurant(this);
    }

    public void removeMenu(Menu menu) {
        menus.remove(menu);
        menu.setRestaurant(null);
    }
}
