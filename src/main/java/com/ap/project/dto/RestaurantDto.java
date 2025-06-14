package com.ap.project.dto;

import lombok.Getter;

@Getter
public class RestaurantDto {
    private String name;
    private String address;
    private String phone;
    private String logoBase64;
    private Integer tax_fee;
    private Integer additional_fee;
    private WorkingHourDto working_hour;
}
