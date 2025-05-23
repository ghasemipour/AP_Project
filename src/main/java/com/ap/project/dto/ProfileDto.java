package com.ap.project.dto;

import com.ap.project.Enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileDto {
    String name;
    String phoneNumber;
    String password;
    String address;
    String profileImageBase64;

    public ProfileDto(String name,
                      String phoneNumber,
                      String password,
                      String address,
                      String profileImageBase64) {

    }
}
