package com.ap.project.dto;

import com.ap.project.Enums.UserRole;
import com.ap.project.entity.general.BankAccount;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileDto {
    private String full_name;
    private String phone;
    private String password;
    private UserRole role;
    private String address;
    private String profileImageBase64;
    private String email;
    private BankAccountDto bank_info;
    private String brandInfo;
    private String discription;


    public ProfileDto(String name, String phoneNumber, String email, String profilePicture, String address, BankAccount bankAccount, String discription, String brandInfo, UserRole role) {
        this.full_name = name;
        this.phone = phoneNumber;
        this.email = email;
        this.profileImageBase64 = profilePicture;
        this.address = address;
        if(bankAccount != null) {
            this.bank_info = new BankAccountDto(bankAccount.getBankName(), bankAccount.getAccountNumber());
        }
        this.discription = discription;
        this.brandInfo = brandInfo;
        this.role = role;

    }
}
