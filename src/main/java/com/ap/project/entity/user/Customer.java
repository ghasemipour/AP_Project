package com.ap.project.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Customer extends User{

    @Column(nullable = false)
    private String address;
}
