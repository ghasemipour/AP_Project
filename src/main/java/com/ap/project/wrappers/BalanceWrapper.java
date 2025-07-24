package com.ap.project.wrappers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BalanceWrapper {
    private Double balance;

    public BalanceWrapper(double balance) {
        this.balance = balance;
    }
}
