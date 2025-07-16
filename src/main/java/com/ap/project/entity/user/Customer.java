package com.ap.project.entity.user;

import com.ap.project.Enums.UserRole;
import com.ap.project.dao.TransactionDao;
import com.ap.project.dto.ProfileDto;
import com.ap.project.entity.general.Transaction;
import com.ap.project.entity.general.Wallet;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.restaurant.Rating;
import com.ap.project.entity.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Customer extends User implements HasAddress {

    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Rating> ratings = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "favorite_restaurants",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> favoriteRestaurants = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    private Wallet wallet;

    public Customer(String name, String number, String password, String email, String profilePricture, String address) {
        super(name, number, password, email, profilePricture);
        this.address = address;
        Wallet wallet = new Wallet();
        TransactionDao.saveWallet(wallet, this.getUserId());
        this.wallet = wallet;
    }

    public Customer() {
//        Wallet wallet = new Wallet();
//        TransactionDao.saveWallet(wallet, this.getUserId());
//        this.wallet = wallet;
    }

    @Override
    public ProfileDto getProfile() {
        ProfileDto profileDto = new ProfileDto(this.getName(), this.getPhoneNumber(), this.getEmail(), this.getProfilePicture(), this.address, null, null, null, UserRole.CUSTOMER);
        return profileDto;
    }

    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setUser(null);
    }

    public void addRating(Rating rating) {
        ratings.add(rating);
        rating.setUser(this);
    }

    public void removeRating(Rating rating) {
        ratings.remove(rating);
        rating.setUser(null);
    }

    public void addFavoriteRestaurant(Restaurant restaurant) {
        if (!favoriteRestaurants.contains(restaurant)) {
            favoriteRestaurants.add(restaurant);
        }
    }

    public void removeRestaurantFromFavorites(Restaurant restaurant) {
        favoriteRestaurants.remove(restaurant);
    }

    public void addTransaction(Transaction newTransaction) {
        transactions.add(newTransaction);
    }
}
