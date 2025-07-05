package com.ap.project.Exceptions;

public class NoSuchRestaurant extends RuntimeException {
    public NoSuchRestaurant(String message) {
        super(message);
    }
}
