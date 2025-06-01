package com.ap.project.httpHandler;

import com.ap.project.dao.UserDao;
import com.ap.project.entity.user.User;
import com.ap.project.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class SuperHttpHandler {

    static User getUserByExchange(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(401, -1);
            return null;
        }
        String token = authHeader.substring(7).trim();
        if(token == null) {
            exchange.sendResponseHeaders(401, -1);
            return null;
        }

        String userId = JwtUtil.validateToken(token);
        if(userId == null) {
            exchange.sendResponseHeaders(401, -1);
            return null;
        }

        User user = UserDao.getUserById(userId);
        if(user == null) {
            exchange.sendResponseHeaders(404, -1);
            return null;
        }

        return user;
    }
}
