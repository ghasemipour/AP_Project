package com.ap.project.httpHandler;

import com.ap.project.Enums.UserRole;
import com.ap.project.dto.RegisterDto;

import com.ap.project.dto.RegisterResponseDto;
import com.ap.project.entity.general.BankAccount;
import com.ap.project.entity.user.Courier;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.Seller;
import com.ap.project.entity.user.User;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RegisterHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        RegisterDto req = new Gson().fromJson(reader, RegisterDto.class);

        if(req.getName() == null || req.getPassword() == null || req.getRole() == null || req.getPhoneNumber() == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }
        if(req.getRole().equals(UserRole.CUSTOMER) && req.getAddress() == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }
        if(req.getRole().equals(UserRole.SELLER) && (req.getAddress() == null || (req.getBankAccount().getBankName() == null || req.getBankAccount().getAccountNumber() == null))){
            exchange.sendResponseHeaders(400, -1);
            return;
        }
        if(req.getRole().equals(UserRole.COURIER) && (req.getBankAccount().getBankName() == null || req.getBankAccount().getAccountNumber() == null)) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        //Checking phoneNumber format
        final String phoneNumberRegex = "^09[0-9]{11}$";
        if(!req.getPhoneNumber().matches(phoneNumberRegex)) {
            String response = "Invalid phone number";
            byte [] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
            return;
        }

        //Checking Email format
        final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9+_.-]+$";
        if(req.getEmail() != null && !req.getEmail().matches(emailRegex)) {
            String response = "Invalid email";
            byte [] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
            return;
        }

        if(com.ap.project.dao.UserDao.IsPhoneNumberTaken(req.getPhoneNumber())){
            String response = "Phone number already exists";
            byte [] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(409, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
            return;
        }

        if(req.getEmail() != null && com.ap.project.dao.UserDao.IsEmailTaken(req.getEmail())){
            String response = "Email already exists";
            byte [] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(409, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
            return;
        }

        User user = switch (req.getRole()) {
            case CUSTOMER ->
                    new Customer(req.getPhoneNumber(), req.getEmail(), req.getPassword(), req.getEmail(), req.getProfileImageBase64(), req.getAddress());
            case COURIER ->
                    new Courier(req.getName(), req.getPhoneNumber(), req.getPassword(), req.getEmail(), req.getProfileImageBase64(), new BankAccount(req.getBankAccount().getBankName(), req.getBankAccount().getAccountNumber()));
            case SELLER ->
                    new Seller(req.getName(), req.getPhoneNumber(), req.getPassword(), req.getEmail(), req.getProfileImageBase64(), req.getAddress(), new BankAccount(req.getBankAccount().getBankName(), req.getBankAccount().getAccountNumber()));
        };

        com.ap.project.dao.UserDao.saveUser(user);
        RegisterResponseDto response = new RegisterResponseDto(
                "User registered successfully",
                user.getUserId(),
                com.ap.project.util.JwtUtil.generateToken(user.getUserId())
        );

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(response);

        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();

    }
}
