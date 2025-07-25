package com.ap.project.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
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
    private double avg_rating;

    public FoodDto(
            String name,
            String description,
            int price,
            int supply,
            List<String> keywords,
            int vendor_id, 
            String imageBase64,
            int id,
            List<Integer> ratings
    ) {
        this.name = name;
        this.description = description;
        this.imageBase64 = imageBase64;
        this.price = price;
        this.supply = supply;
        this.keywords = keywords;
        this.vendor_id = vendor_id;
        this.id = id;
        if (ratings != null && !ratings.isEmpty()) {
            double avg = 0;
            for (Integer rating : ratings) {
                avg += rating;
            }
            avg /= ratings.size();
            avg_rating = avg;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FoodDto [name=");
        sb.append(name);
        System.out.println(sb.toString());
        sb.append(", description=");
        sb.append(description);
        System.out.println(sb.toString());
        sb.append(", price=");
        sb.append(price);
        System.out.println(sb.toString());
        sb.append(", supply=");
        sb.append(supply);
        System.out.println(sb.toString());
        if(keywords != null) {
            sb.append(", keywords=");
            sb.append(keywords.toString());
            System.out.println(sb.toString());
        }
        sb.append(", vendor_id=");
        sb.append(vendor_id);
        System.out.println(sb.toString());
        sb.append(", imageBase64=");
        sb.append(imageBase64);
        System.out.println(sb.toString());
        sb.append(", id=");
        sb.append(id);
        System.out.println(sb.toString());
        sb.append("]");
        return sb.toString();
    }
}
