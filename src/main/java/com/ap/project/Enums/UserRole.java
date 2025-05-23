package com.ap.project.Enums;

public enum UserRole {
    CUSTOMER("buyer"),
    SELLER("seller"),
    COURIER("courier");

    private final String roleName;
    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static UserRole fromString(String roleName) {
        for(UserRole role : values()) {
            if(role.roleName.equals(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role name: " + roleName);
    }
}
