package com.ap.project.entity.user;

import com.ap.project.Enums.UserRole;
import com.ap.project.dto.ProfileDto;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Admin extends User{

    public Admin(String name, String phone, String password, String email) {
        super(name, phone, password, email, null);
    }

    public Admin() {

    }

    @Override
    public ProfileDto getProfile() {
        return new ProfileDto(this.getName(), this.getPhoneNumber(), this.getEmail(), this.getProfilePicture(), null, null, null, null, UserRole.ADMIN);
    }
}
