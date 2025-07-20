package com.ap.project.dto;

import com.ap.project.Enums.UserRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumeratedValue;

public class LoginResponseDto {
    private String massage;
    private int user_id;
    private String token;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String name;
    private String profileImageBase64;

    public LoginResponseDto(String massage, int user_id, String token, UserRole role, String name, String profileImageBase64) {
        this.massage = massage;
        this.user_id = user_id;
        this.token = token;
        this.role = role;
        this.name = name;
        this.profileImageBase64 = profileImageBase64;
    }
}
