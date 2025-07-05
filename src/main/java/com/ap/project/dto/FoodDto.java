package com.ap.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FoodDto {
    private String name;
    private String description;
    private Integer price;
    private Integer supply;
    private List<String> keywords;
    private int vendor_id;
    private String imageBase64;
    private int id;

    public FoodDto(
            String name,
            String description,
            int price,
            int supply,
            List<String> keywords,
            int vendor_id,
            String imageBase64,
            int id
    ) {
        this.name = name;
        this.description = description;
        this.imageBase64 = imageBase64;
        this.price = price;
        this.supply = supply;
        this.keywords = keywords;
        this.vendor_id = vendor_id;
        this.id = id;
    }
}
