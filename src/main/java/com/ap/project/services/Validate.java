package com.ap.project.services;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Validate {
    private static final String phoneNumberRegex = "^09[0-9]{9}$";
    private static final String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9+_.-]+$";

    public static boolean validatePhone(String phone, HttpExchange exchange) throws IOException {
         if(!phone.matches(phoneNumberRegex)){
             String response = "{\"error\": \"Invalid phone number\"}";
             byte [] responseBytes = response.getBytes(StandardCharsets.UTF_8);
             exchange.sendResponseHeaders(400, responseBytes.length);
             OutputStream os = exchange.getResponseBody();
             os.write(responseBytes);
             os.close();
             return false;
         }
         return true;
    }

    public static boolean validateEmail(String email, HttpExchange exchange) throws IOException {
        if(email == null || email.isEmpty()){
            return true;
        }
        if(!email.matches(emailRegex)){
            String response = "{\"error\": \"Invalid email\"}";
            byte [] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
            return false;
        }
        return true;
    }
}
