package com.ap.project.Exceptions;

public class NoSuchRating extends RuntimeException {
    public NoSuchRating(String message) {
        super(message);
    }
}
