package com.ap.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RestaurantReportDto {
    private List<RestaurantReportResponseDto> reportList;
    private String bestItem;
    private Double totalIncome;

    public RestaurantReportDto(List<RestaurantReportResponseDto> dtoList, String bestItem, double totalIncome) {
        this.reportList = dtoList;
        this.bestItem = bestItem;
        this.totalIncome = totalIncome;
    }
}
