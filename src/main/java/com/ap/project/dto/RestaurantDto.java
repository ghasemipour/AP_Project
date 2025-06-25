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
    private String id;

    public RestaurantDto(String id, String name, String address, String phone, String logoBase64, Integer tax_fee, Integer additional_fee, WorkingHourDto working_hour ) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logoBase64 = logoBase64;
        this.tax_fee = tax_fee;
        this.additional_fee = additional_fee;
        this.working_hour = working_hour;
        this.id = id;

    }
}
