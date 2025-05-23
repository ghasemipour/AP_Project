package com.ap.project.dto;

import com.ap.project.Enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDto {
    private String full_name;
    private String phone;
    private String password;
    private UserRole role;
    private String address;
    private String profileImageBase64;
    private String email;
    private BankAccountDto bank_info;

    public RegisterDto() {}

}
