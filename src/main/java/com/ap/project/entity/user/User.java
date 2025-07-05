package com.ap.project.entity.user;

import com.ap.project.dto.ProfileDto;
import com.ap.project.util.PasswordUtil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int userId;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String phoneNumber;
    @Column(nullable = false)
    private String password;

    private String profilePicture;
    @Column(unique = true)
    private String email;

    public User(String name, String number, String password, String email, String profilePicture) {
        this.name = name;
        this.phoneNumber = number;
        this.password = PasswordUtil.hashPassword(password);
        this.profilePicture = profilePicture;
        this.email = email;
    }

    public User() {

    }

    public abstract ProfileDto getProfile();
}
