package com.ap.project.entity.restaurant;

import com.ap.project.dto.MenuDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Menu{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "menu_food",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private List<Food> foodItems = new ArrayList<>();

    public Menu(MenuDto menuDto) {
        this.title = menuDto.getTitle();
    }

    public Menu() {

    }

    public void addFoodItem(Food food) {
        foodItems.add(food);
    }

    public void removeFoodItem(Food food) {
        foodItems.remove(food);
    }
}
