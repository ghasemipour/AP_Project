package com.ap.project.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JwtBlacklist {
    private static final ConcurrentMap<String, Long> blacklist = new ConcurrentHashMap<>();

    public static void blacklistToken(String token, long expiryTimeMillis) {
        blacklist.put(token, expiryTimeMillis);
    }

    public static boolean isBlacklisted(String token) {
        Long expiry = blacklist.get(token);
        if (expiry == null) return false;

        if (System.currentTimeMillis() > expiry) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    public static void logout(String token, long expiryTimeMillis) {
        blacklistToken(token, expiryTimeMillis);
    }

    public static void cleanup() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
