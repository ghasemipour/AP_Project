package com.ap.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscountDto {
    private Integer percentage;

    public DiscountDto(Integer percentage) {
        this.percentage = percentage;
    }
}
