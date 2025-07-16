package com.ap.project.entity.general;

import com.ap.project.entity.user.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    double balance;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Customer customer;

    public void topUp(double amount){
        balance += amount;
    }

    public void withdraw(double amount){
        if(balance < amount){
            throw new RuntimeException("Insufficient balance");
        }
        balance -= amount;
    }

}
