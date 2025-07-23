package com.ap.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuDto {
    private String title;
    private Integer item_id;

    public MenuDto(String title) {
        this.title = title;
    }
}
