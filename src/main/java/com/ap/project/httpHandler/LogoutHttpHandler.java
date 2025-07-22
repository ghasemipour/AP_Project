package com.ap.project.httpHandler;

import com.ap.project.util.JwtBlacklist;
import com.ap.project.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import static com.ap.project.httpHandler.SuperHttpHandler.internalServerFailureError;
import static com.ap.project.httpHandler.SuperHttpHandler.sendSuccessMessage;

public class LogoutHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String authHeader = authHeaders.getFirst();
        if (!authHeader.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String token = authHeader.substring("Bearer ".length());

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(JwtUtil.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            long expiry = claims.getExpiration().getTime();
            JwtBlacklist.logout(token, expiry);
            sendSuccessMessage("User logged out successfully", exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }
}
