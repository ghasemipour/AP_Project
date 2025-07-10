package com.ap.project.entity.restaurant;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    private Order order;

    @ManyToOne(optional = false)
    private Food food;

    private Integer quantity;
}
