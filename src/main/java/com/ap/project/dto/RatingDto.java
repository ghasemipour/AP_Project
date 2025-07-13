package com.ap.project.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RatingDto {
    private Integer order_id;
    @Min(1)
    @Max(5)
    private Integer rating;
    private String comment;
    private List<String> imageBase64 = new ArrayList<>();
    private Integer userId;
    private Integer ratingId;

}
