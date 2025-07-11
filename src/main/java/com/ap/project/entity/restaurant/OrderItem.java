package com.ap.project.entity.restaurant;


import com.ap.project.dto.OrderItemDto;
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

    public OrderItem(Food food, int quantity, Order order) {
        this.food = food;
        this.quantity = quantity;
        this.order = order;
    }

    public OrderItemDto getDto() {
        OrderItemDto orderItemDto = new OrderItemDto();

        orderItemDto.setItem_id(food.getFoodId());
        orderItemDto.setQuantity(quantity);

        return orderItemDto;
    }

}
