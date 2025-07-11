package com.ap.project.Exceptions;

public class NoSuchMenu extends RuntimeException {
    public NoSuchMenu(String message) {
        super(message);
    }
}
