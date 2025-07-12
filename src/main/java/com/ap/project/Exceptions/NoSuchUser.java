package com.ap.project.Exceptions;

public class NoSuchUser extends RuntimeException {
    public NoSuchUser(String message) {
        super(message);
    }
}
