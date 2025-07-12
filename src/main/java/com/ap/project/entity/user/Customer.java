package com.ap.project.entity.user;

import com.ap.project.Enums.UserRole;
import com.ap.project.dto.ProfileDto;
import com.ap.project.entity.restaurant.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Customer extends User implements HasAddress{

    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();


    public Customer(String name, String number, String password, String email, String profilePricture, String address) {
        super(name, number, password, email, profilePricture);
        this.address = address;
    }

    public Customer() {

    }

    @Override
    public ProfileDto getProfile()
    {
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
}
