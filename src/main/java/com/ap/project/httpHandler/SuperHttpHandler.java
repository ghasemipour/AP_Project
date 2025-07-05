package com.ap.project.httpHandler;

import com.ap.project.dao.UserDao;
import com.ap.project.entity.user.User;
import com.ap.project.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SuperHttpHandler {

    static User getUserByExchange(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(401, -1);
            return null;
        }
        String token = authHeader.substring(7).trim();
        if (token == null) {
            exchange.sendResponseHeaders(401, -1);
            return null;
        }

        String userId = JwtUtil.validateToken(token);
        if (userId == null) {
            exchange.sendResponseHeaders(401, -1);
            return null;
        }

        User user = UserDao.getUserById(Integer.parseInt(userId));
        if (user == null) {
            exchange.sendResponseHeaders(401, -1);
            return null;
        }

        return user;
    }

    protected static void sendSuccessMessage(String response, HttpExchange exchange) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected static void internalServerFailureError(Exception e, HttpExchange exchange) throws IOException {
            String errorResponse = "{\"error\": \"Internal server error\"}";
            byte[] responseBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
    }
}
