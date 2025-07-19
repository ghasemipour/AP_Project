package com.ap.project.dto;

import lombok.Getter;

@Getter
public class RegisterResponseDto {
    private String massage;
    private int user_id;
    private String token;
    private String name;

    public RegisterResponseDto(String massage, int user_id, String name, String token) {
        this.massage = massage;
        this.user_id = user_id;
        this.token = token;
        this.name = name;
    }
}
