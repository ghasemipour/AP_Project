package com.ap.project.httpHandler;

import com.ap.project.Enums.UserRole;
import com.ap.project.dao.TransactionDao;
import com.ap.project.dao.UserDao;
import com.ap.project.deserializer.UserRoleDeserializer;
import com.ap.project.dto.RegisterDto;

import com.ap.project.dto.RegisterResponseDto;
import com.ap.project.entity.general.BankAccount;
import com.ap.project.entity.general.Wallet;
import com.ap.project.entity.user.Courier;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.Seller;
import com.ap.project.entity.user.User;
import com.ap.project.services.Validate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static com.ap.project.httpHandler.SuperHttpHandler.internalServerFailureError;
import static com.ap.project.httpHandler.SuperHttpHandler.sendSuccessMessage;
import static com.ap.project.util.JwtUtil.generateToken;

public class RegisterHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(UserRole.class, new UserRoleDeserializer()).create();
            RegisterDto req = gson.fromJson(reader, RegisterDto.class);

            if (req.getFull_name() == null || req.getPassword() == null || req.getRole() == null || req.getPhone() == null) {
                String response = "";
                if (req.getFull_name() == null)
                    response += "{\"error\": \"Name required\"}\n";
                if (req.getPassword() == null)
                    response += "{\"error\": \"Password required\"}\n";
                if (req.getRole() == null)
                    response += "{\"error\": \"Role required\"}\n";
                if (req.getPhone() == null)
                    response += "{\"error\": \"PhoneNumber required\"}\n";

                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            if (req.getRole().equals(UserRole.CUSTOMER) && req.getAddress() == null) {
                String response = "{\"error\": \"Address required\"}\n";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            if (req.getRole().equals(UserRole.SELLER) && (req.getAddress() == null || (req.getBank_info() == null || req.getBank_info().getBank_name() == null || req.getBank_info().getAccount_number() == null))) {
                String response = "";
                if (req.getAddress() == null)
                    response += "{\"error\": \"Address required\"}\n";
                if (req.getBank_info() == null || req.getBank_info().getBank_name() == null)
                    response += "{\"error\": \"Bank name required\"}\n";
                if (req.getBank_info() == null || req.getBank_info().getAccount_number() == null)
                    response += "{\"error\": \"Bank account number required\"}\n";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            if (req.getRole().equals(UserRole.COURIER) && (req.getBank_info() == null || req.getBank_info().getBank_name() == null || req.getBank_info().getAccount_number() == null)) {
                String response = "";
                if (req.getBank_info().getBank_name() == null)
                    response += "{\"error\": \"Bank name required\"}\n";
                if (req.getBank_info().getAccount_number() == null)
                    response += "{\"error\": \"Account number required\"}\n";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            //Checking phoneNumber format
            if (!Validate.validatePhone(req.getPhone(), exchange)) {
                return;
            }

            //Checking Email format

            if (!Validate.validateEmail(req.getEmail(), exchange)) {
                return;
            }

            if (UserDao.IsPhoneNumberTaken(req.getPhone())) {
                String response = "{\"error\": \"Phone number already exists\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(409, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            if (req.getEmail() != null && UserDao.IsEmailTaken(req.getEmail())) {
                String response = "{\"error\": \"Email already exists\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(409, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            User user = switch (req.getRole()) {
                case CUSTOMER ->
                        new Customer(req.getFull_name(), req.getPhone(), req.getPassword(), req.getEmail(), req.getProfileImageBase64(), req.getAddress());
                case COURIER ->
                        new Courier(req.getFull_name(), req.getPhone(), req.getPassword(), req.getEmail(), req.getProfileImageBase64(), new BankAccount(req.getBank_info().getBank_name(), req.getBank_info().getAccount_number()));
                case SELLER ->
                        new Seller(req.getFull_name(), req.getPhone(), req.getPassword(), req.getEmail(), req.getProfileImageBase64(), req.getAddress(), new BankAccount(req.getBank_info().getBank_name(), req.getBank_info().getAccount_number()));
                case ADMIN -> null;
            };

            if (user == null) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            UserDao.saveUser(user);
            if (user instanceof Customer) ((Customer) user).setWallet(new Wallet());


            String token = generateToken(user.getUserId());

            RegisterResponseDto response = new RegisterResponseDto(
                    "User registered successfully",
                    user.getUserId(),
                    token
            );

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            sendSuccessMessage(gson.toJson(response), exchange);

        }
        catch (Exception e) {
            internalServerFailureError(e,exchange);
        }
    }
}
