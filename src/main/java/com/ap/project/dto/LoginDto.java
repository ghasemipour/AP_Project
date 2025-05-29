package com.ap.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {
    private String phone;
    private String password;

    public LoginDto() {};
}
