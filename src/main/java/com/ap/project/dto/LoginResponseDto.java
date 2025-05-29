package com.ap.project.dto;

public class LoginResponseDto {
    private String massage;
    private String user_id;
    private String token;

    public LoginResponseDto(String massage, String user_id, String token) {
        this.massage = massage;
        this.user_id = user_id;
        this.token = token;
    }
}
