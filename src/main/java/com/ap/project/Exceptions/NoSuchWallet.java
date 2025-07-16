package com.ap.project.Exceptions;

public class NoSuchWallet extends RuntimeException {
    public NoSuchWallet(String message) {
        super(message);
    }
}
