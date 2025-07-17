package com.ap.project.entity.user;

import com.ap.project.Enums.ApprovalStatus;
import com.ap.project.Enums.UserRole;
import com.ap.project.dto.ProfileDto;
import com.ap.project.entity.general.BankAccount;
import com.ap.project.entity.restaurant.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Courier extends User implements HasBankAccount, NeedApproval {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private BankAccount bankAccount;

    @OneToMany(mappedBy = "courier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    ApprovalStatus approvalStatus = ApprovalStatus.WAITING;

    public Courier(String name, String number, String password, String email, String profilePicture, BankAccount bankAccount) {
        super(name, number, password, email, profilePicture);
        this.bankAccount = bankAccount;
    }

    public Courier() {

    }

    @Override
    public ProfileDto getProfile()
    {
        ProfileDto profileDto = new ProfileDto(this.getName(), this.getPhoneNumber(), this.getEmail(), this.getProfilePicture(), null, this.bankAccount, null, null, UserRole.COURIER, approvalStatus);
        return profileDto;
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    @Override
    public void changeStatus(ApprovalStatus status) {
        approvalStatus = status;
    }
}
