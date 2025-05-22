package com.ap.project.entity.user;

import com.ap.project.general.BankAccount;

import java.util.ArrayList;

public class Seller extends User{
    private String address;
    private BankAccount bankAccount;
    private ArrayList<String> discription = new ArrayList<>();
}
