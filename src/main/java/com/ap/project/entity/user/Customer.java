package com.ap.project.entity.user;

import com.ap.project.Enums.UserRole;
import com.ap.project.dto.ProfileDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Customer extends User implements HasAddress{

    @Column(nullable = false)
    private String address;

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
}
