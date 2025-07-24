package com.ap.project.dto;

import com.ap.project.Enums.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryDto {
    Integer orderId;
    String deliveryAddress;
    String buyerName;
    String buyerPhone;
    String restaurantName;
    String restaurantPhone;
    String restaurantAddress;
    Status status;

    public DeliveryDto(int id, String deliveryAddress, String name, String phoneNumber, String name1, String address, String phone, Status status) {
        this.orderId = id;
        this.deliveryAddress = deliveryAddress;
        this.buyerName = name;
        this.buyerPhone = phoneNumber;
        this.restaurantName = name1;
        this.restaurantPhone = phone;
        this.status = status;
        this.restaurantAddress = address;
    }
}
