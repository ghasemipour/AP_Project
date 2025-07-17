package com.ap.project.entity.user;

import com.ap.project.Enums.ApprovalStatus;
import com.ap.project.Enums.UserRole;
import com.ap.project.dao.RestaurantDao;
import com.ap.project.dto.ProfileDto;
import com.ap.project.dto.RestaurantDto;
import com.ap.project.entity.general.BankAccount;
import com.ap.project.entity.restaurant.Restaurant;
import com.sun.net.httpserver.HttpExchange;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Seller extends User implements HasAddress, HasBankAccount, NeedApproval{

    @Column(nullable = false)
    private String address;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private BankAccount bankAccount;
    @Column(columnDefinition = "text")
    private String discription;

    private String brandInfo;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Restaurant> restaurants = new ArrayList<>();

    ApprovalStatus approvalStatus = ApprovalStatus.WAITING;

    public Seller(String name, String number, String password, String email, String profilePicture, String address, BankAccount bankAccount) {
        super(name, number, password, email, profilePicture);
        this.address = address;
        this.bankAccount = bankAccount;
    }

    public Seller() {

    }

    @Override
    public ProfileDto getProfile()
    {
        ProfileDto profileDto = new ProfileDto(this.getName(), this.getPhoneNumber(), this.getEmail(), this.getProfilePicture(), this.address, this.bankAccount, this.discription, this.brandInfo, UserRole.SELLER, approvalStatus);
        return profileDto;
    }

    public void addRestaurant(Restaurant restaurant) {
        restaurants.add(restaurant);
        restaurant.setOwner(this);
    }

    public List<RestaurantDto> getDtoRestaurants(HttpExchange exchange) {
        restaurants = RestaurantDao.getRestaurantsBySellerId(this.userId, exchange);
        System.out.println(restaurants);
        List<RestaurantDto> restaurantDtos = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            restaurantDtos.add(restaurant.GetDto());
        }
        return restaurantDtos;
    }

    @Override
    public void changeStatus(ApprovalStatus status) {
        approvalStatus = status;
    }
}
