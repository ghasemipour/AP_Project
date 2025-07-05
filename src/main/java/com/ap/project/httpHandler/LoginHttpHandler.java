package com.ap.project.httpHandler;

import com.ap.project.dao.UserDao;
import com.ap.project.dto.LoginDto;
import com.ap.project.dto.LoginResponseDto;
import com.ap.project.entity.user.User;
import com.ap.project.services.Validate;
import com.ap.project.util.JwtUtil;
import com.ap.project.util.PasswordUtil;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Session;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static com.ap.project.httpHandler.SuperHttpHandler.internalServerFailureError;
import static com.ap.project.httpHandler.SuperHttpHandler.sendSuccessMessage;


public class LoginHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            LoginDto req = new Gson().fromJson(reader, LoginDto.class);
            if (req.getPhone() == null || req.getPhone().isEmpty() || req.getPassword().isEmpty() || req.getPassword() == null) {
                String response = "";
                if (req.getPhone() == null || req.getPhone().isEmpty())
                    response += "{\"error\": \"PhoneNumber required\"}\n";
                if (req.getPassword() == null || req.getPassword().isEmpty())
                    response += "{\"error\": \"password required\"}\n";

                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            if (!Validate.validatePhone(req.getPhone(), exchange)) {
                return;
            }
            User user = UserDao.getUserByPhone(req.getPhone());
            if (user == null) {
                String response = "{\"error\": \"User does not exist\"}\n";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(401, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            if (!user.getPassword().equals(PasswordUtil.hashPassword(req.getPassword()))) {
                String response = "{\"error\": \"Incorrect password\"}\n";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(401, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            String token = JwtUtil.generateToken(user.getUserId());

            LoginResponseDto loginResponseDto = new LoginResponseDto("User logged in successfully",
                    user.getUserId(),
                    token);

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            sendSuccessMessage(new Gson().toJson(loginResponseDto), exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }
}


