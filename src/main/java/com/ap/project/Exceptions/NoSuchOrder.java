package com.ap.project.Exceptions;

public class NoSuchOrder extends RuntimeException {
    public NoSuchOrder(String message) {
        super(message);
    }
}
