package com.ap.project.entity.user;

import com.ap.project.Enums.UserRole;
import com.ap.project.dto.ProfileDto;
import com.ap.project.entity.general.BankAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
public class Seller extends User implements HasAddress, HasBankAccount{

    @Column(nullable = false)
    private String address;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private BankAccount bankAccount;
    @Column(columnDefinition = "text")
    private String discription;

    private String brandInfo;

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
        ProfileDto profileDto = new ProfileDto(this.getName(), this.getPhoneNumber(), this.getEmail(), this.getProfilePicture(), this.address, this.bankAccount, this.discription, this.brandInfo, UserRole.SELLER);
        return profileDto;
    }
}
