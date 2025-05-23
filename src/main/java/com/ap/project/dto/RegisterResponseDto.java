package com.ap.project.dto;

import lombok.Getter;

@Getter
public class RegisterResponseDto {
    private String massage;
    private String user_id;
    private String token;

    public RegisterResponseDto(String massage, String user_id, String token) {
        this.massage = massage;
        this.user_id = user_id;
        this.token = token;
    }
}
