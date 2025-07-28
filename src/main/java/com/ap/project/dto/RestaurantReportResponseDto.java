package com.ap.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantReportResponseDto {
    private String date;
    private Double totalSales;

    public RestaurantReportResponseDto(String date, double sale) {
        this.date = date;
        this.totalSales = sale;
    }
}
