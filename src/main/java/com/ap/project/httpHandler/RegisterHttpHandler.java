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

import static com.ap.project.util.JwtUtil.generateToken;

public class RegisterHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        RegisterDto req = new Gson().fromJson(reader, RegisterDto.class);

        if(req.getFull_name() == null || req.getPassword() == null || req.getRole() == null || req.getPhone() == null) {
            String response = "";
            if(req.getFull_name() == null)
                response += "Name required\n";
            if(req.getPassword() == null)
                response += "Password required\n";
            if(req.getRole() == null)
                response += "Role required\n";
            if(req.getPhone() == null)
                response += "PhoneNumber required\n";

            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
            return;
        }
        if(req.getRole().equals(UserRole.CUSTOMER) && req.getAddress() == null) {
            String response = "Address required\n";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(400, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
            return;
        }
        if(req.getRole().equals(UserRole.SELLER) && (req.getAddress() == null || (req.getBank_info().getBankName() == null || req.getBank_info().getAccountNumber() == null))){
            String response = "";
            if(req.getAddress() == null)
                response += "Address required\n";
            if(req.getBank_info().getBankName() == null)
                response += "Bank account required\n";
            if(req.getBank_info().getAccountNumber() == null)
                response += "Account number required\n";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(400, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
            return;
        }
        if(req.getRole().equals(UserRole.COURIER) && (req.getBank_info().getBankName() == null || req.getBank_info().getAccountNumber() == null)) {
            String response = "";
            if(req.getBank_info().getBankName() == null)
                response += "Bank account required\n";
            if(req.getBank_info().getAccountNumber() == null)
                response += "Account number required\n";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
            return;
        }

        //Checking phoneNumber format
        final String phoneNumberRegex = "^09[0-9]{9}$";
        if(!req.getPhone().matches(phoneNumberRegex)) {
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

        if(com.ap.project.dao.UserDao.IsPhoneNumberTaken(req.getPhone())){
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
                    new Customer(req.getFull_name(),req.getPhone(), req.getPassword(), req.getEmail(), req.getProfileImageBase64(), req.getAddress());
            case COURIER ->
                    new Courier(req.getFull_name(), req.getPhone(), req.getPassword(), req.getEmail(), req.getProfileImageBase64(), new BankAccount(req.getBank_info().getBankName(), req.getBank_info().getAccountNumber()));
            case SELLER ->
                    new Seller(req.getFull_name(), req.getPhone(), req.getPassword(), req.getEmail(), req.getProfileImageBase64(), req.getAddress(), new BankAccount(req.getBank_info().getBankName(), req.getBank_info().getAccountNumber()));
        };

        com.ap.project.dao.UserDao.saveUser(user);


        String token = generateToken(user.getUserId());
        System.out.println(token);

        RegisterResponseDto response = new RegisterResponseDto(
                "User registered successfully",
                user.getUserId(),
                token
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
