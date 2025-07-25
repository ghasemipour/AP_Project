package com.ap.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MenuDto {
    private String title;
    private Integer item_id;
    private List<FoodDto> items;

    public MenuDto(String title) {
        this.title = title;
    }

    public MenuDto(String title, List<FoodDto> items) {
        this.title = title;
        this.items = items;
    }
}
