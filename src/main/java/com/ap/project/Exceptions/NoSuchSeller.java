package com.ap.project.Exceptions;

public class NoSuchSeller extends RuntimeException {
    public NoSuchSeller(String message) {
        super(message);
    }
}
