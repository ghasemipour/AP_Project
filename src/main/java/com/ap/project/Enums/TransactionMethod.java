package com.ap.project.Enums;

public enum TransactionMethod {
    WALLET("wallet"),
    ONLINE("online");


    private final String method;

    TransactionMethod(String method) {
        this.method = method;
    }
}
