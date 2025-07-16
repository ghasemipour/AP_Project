package com.ap.project.Enums;

public enum TransactionMethod {
    WALLET("wallet"),
    ONLINE("online");


    private final String method;

    TransactionMethod(String method) {
        this.method = method;
    }

    public static TransactionMethod fromString(String method) {
        if (method == null) return null;
        for (TransactionMethod m : TransactionMethod.values()) {
            if (m.method.equalsIgnoreCase(method)) {
                return m;
            }
        }
        return null;
    }
}
