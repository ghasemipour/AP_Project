package com.ap.project.Exceptions;

public class NoSuchFoodItem extends RuntimeException {
    public NoSuchFoodItem(String message) {
        super(message);
    }
}
