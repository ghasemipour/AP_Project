package com.ap.project.Exceptions;

public class NoSuchCoupon extends RuntimeException {
    public NoSuchCoupon(String message) {
        super(message);
    }
}
