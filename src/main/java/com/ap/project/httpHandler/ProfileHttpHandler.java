package com.ap.project.httpHandler;

import com.ap.project.dao.UserDao;
import com.ap.project.dto.ProfileDto;
import com.ap.project.entity.user.User;
import com.ap.project.services.Validate;
import com.ap.project.util.JwtUtil;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ProfileHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!exchange.getRequestMethod().equals("GET") || !exchange.getRequestMethod().equals("PuT")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if(token == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String userId = JwtUtil.validateToken(token);
        if(userId == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        User user = UserDao.getUserById(userId);
        if(user == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        if(exchange.getRequestMethod().equals("GET")) {
            handleGetProfile(user, exchange);
            return;
        }

        if(exchange.getRequestMethod().equals("PUT")) {
            handleUpdateProfile(user, exchange);

        }
    }

    public void handleGetProfile(User user, HttpExchange exchange) throws IOException {
        ProfileDto profileDto = user.getProfile();
        if(profileDto == null) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String response = new Gson().toJson(profileDto);
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    public void handleUpdateProfile(User user, HttpExchange exchange) throws IOException {

        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
        ProfileDto newProfile = new Gson().fromJson(isr, ProfileDto.class);

        if(newProfile.getPhone() != null) {
            if(!Validate.validatePhone(newProfile.getPhone(), exchange)) {
                return;
            }
            if(UserDao.IsPhoneNumberTaken(newProfile.getPhone()) && !user.getPhoneNumber().equals(newProfile.getPhone())) {
                String response = "{\"error\": \"Phone number already exists\"}";
                byte [] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(409, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
                return;
            }
        }

        if(newProfile.getEmail() != null) {
            if(!Validate.validateEmail(newProfile.getEmail(), exchange)) {
                return;
            }

            if(UserDao.IsEmailTaken(newProfile.getEmail()) && !user.getEmail().equals(newProfile.getEmail())) {
                String response = "{\"error\": \"Email already exists\"}";
                byte [] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(409, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
                return;

            }
        }

        UserDao.updateUser(user.getUserId(), newProfile);

        String response = "Profile updated successfully";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();

    }
}
