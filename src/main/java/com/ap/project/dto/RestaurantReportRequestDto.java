package com.ap.project.dto;

import com.ap.project.entity.restaurant.Restaurant;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RestaurantReportRequestDto {
    private String startDate;
    private String endDate;
    private List<String> keywords;

    public RestaurantReportRequestDto(){

    }
}
