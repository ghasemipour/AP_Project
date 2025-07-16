package com.ap.project.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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

    private String created_at;
    private String updated_at;

    public RatingDto(int order_id, int rating, String comment, List<String> imageBase64, int userId, int ratingId, LocalDateTime created_at, LocalDateTime updated_at) {
        this.order_id = order_id;
        this.rating = rating;
        this.comment = comment;
        this.imageBase64 = imageBase64;
        this.userId = userId;
        this.ratingId = ratingId;
        this.created_at = created_at.toString();
        if (updated_at != null)
            this.updated_at = updated_at.toString();
    }
}
