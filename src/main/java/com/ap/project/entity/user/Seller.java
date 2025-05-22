package com.ap.project.entity.user;

import com.ap.project.entity.general.BankAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Entity
@Getter
@Setter
public class Seller extends User{

    @Column(nullable = false)
    private String address;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private BankAccount bankAccount;
    @Column(columnDefinition = "text")
    private String discription;
}
