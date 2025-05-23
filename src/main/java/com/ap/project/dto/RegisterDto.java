package com.ap.project.dto;

import com.ap.project.Enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDto {
    String name;
    String phoneNumber;
    String password;
    UserRole role;
    String address;
    String profileImageBase64;
    BankAccountDto bankAccount;

    public RegisterDto() {}

}
