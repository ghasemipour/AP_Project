package com.ap.project.httpHandler;

import com.ap.project.dao.UserDao;
import com.ap.project.dto.ProfileDto;
import com.ap.project.entity.user.User;
import com.ap.project.services.Validate;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ProfileHttpHandler extends SuperHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!exchange.getRequestMethod().equals("GET") && !exchange.getRequestMethod().equals("PUT")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        User user = getUserByExchange(exchange);
        if(user == null) {
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
        try {
            ProfileDto profileDto = user.getProfile();
            if (profileDto == null) {
                exchange.sendResponseHeaders(401, -1);
                return;
            }

            sendSuccessMessage(new Gson().toJson(profileDto), exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleUpdateProfile(User user, HttpExchange exchange) throws IOException {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
            ProfileDto newProfile = new Gson().fromJson(isr, ProfileDto.class);

            if (newProfile.getPhone() != null) {
                if (!Validate.validatePhone(newProfile.getPhone(), exchange)) {
                    return;
                }
                if (UserDao.IsPhoneNumberTaken(newProfile.getPhone()) && !user.getPhoneNumber().equals(newProfile.getPhone())) {
                    String response = "{\"error\": \"Phone number already exists\"}";
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(409, responseBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();
                    return;
                }
            }

            if (newProfile.getEmail() != null) {
                if (!Validate.validateEmail(newProfile.getEmail(), exchange)) {
                    return;
                }

                if (UserDao.IsEmailTaken(newProfile.getEmail()) && !user.getEmail().equals(newProfile.getEmail())) {
                    String response = "{\"error\": \"Email already exists\"}";
                    byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(409, responseBytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(responseBytes);
                    os.close();
                    return;

                }
            }

            UserDao.updateUser(user.getUserId(), newProfile);

            sendSuccessMessage("Profile updated successfully", exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }

    }
}
