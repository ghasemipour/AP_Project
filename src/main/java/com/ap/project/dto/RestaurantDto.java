package com.ap.project.dto;

import lombok.Getter;

@Getter
public class RestaurantDto {
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
    private int tax_fee;
    private int additional_fee;
    private WorkingHourDto working_hour;
}
