package com.ap.project.dto;

import lombok.Getter;

@Getter
public class RegisterResponseDto {
    private String massage;
    private int user_id;
    private String token;
    private String name;
    private String profilePictureBase64;

    public RegisterResponseDto(String massage, int user_id, String name, String profilePictureBase64, String token) {
        this.massage = massage;
        this.user_id = user_id;
        this.token = token;
        this.name = name;
        this.profilePictureBase64 = profilePictureBase64;
    }
}
