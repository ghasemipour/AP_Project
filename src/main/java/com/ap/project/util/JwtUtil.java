package com.ap.project.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "Fk3LpQDFsXnZGr8R5gcC1N1FbNUjRYXyLdpQs5Q49kQ=";
    private static final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    private static final long EXPIRATION_MS = 3 * 60 * 60 * 1000 * 10; // 3 hours

    public static String generateToken(int userId) {
        try {
            return Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String validateToken(String token) {
        try {

            if (JwtBlacklist.isBlacklisted(token)) {
                System.out.println("Token is blacklisted");
                return null;
            }

            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Key getKey() {
        return key;
    }
}
