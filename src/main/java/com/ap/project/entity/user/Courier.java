package com.ap.project.entity.user;

import com.ap.project.entity.general.BankAccount;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Courier extends User{

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private BankAccount bankAccount;

    public Courier(String name, String number, String password, String email, String profilePicture, BankAccount bankAccount) {
        super(name, number, password, email, profilePicture);
        this.bankAccount = bankAccount;
    }

    public Courier() {

    }
}
