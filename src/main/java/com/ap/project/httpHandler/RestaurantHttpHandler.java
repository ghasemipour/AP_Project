package com.ap.project.httpHandler;

import com.ap.project.dao.RestaurantDao;
import com.ap.project.dao.UserDao;
import com.ap.project.dto.LoginDto;
import com.ap.project.dto.RestaurantDto;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Seller;
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
import java.util.ArrayList;
import java.util.List;

public class RestaurantHttpHandler extends SuperHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = getUserByExchange(exchange);
        if(user == null) {
            return;
        }

        if(!(user instanceof Seller)){
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        String path =exchange.getRequestURI().getPath();
        if(path.equals("/restaurants")) {
            handleAddNewRestaurant(exchange, user);
        }
        else if(path.equals("/restaurants/mine")) {
            handleGetSellersRestaurants(exchange, user);
        }

    }

    private void handleAddNewRestaurant(HttpExchange exchange, User user) throws IOException {
        try {
            if(!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            RestaurantDto req = new Gson().fromJson(reader, RestaurantDto.class);

            if(req.getName() == null || req.getPhone() == null || req.getAddress() == null) {
                String response = "";
                if (req.getName() == null)
                    response += "{\"error\": \"Name required\"}\n";
                if (req.getAddress() == null)
                    response += "{\"error\": \"Address required\"}\n";
                if (req.getPhone() == null)
                    response += "{\"error\": \"PhoneNumber required\"}\n";

                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            if(!Validate.validatePhone(req.getPhone(), exchange)) {
                return;
            }

            if(RestaurantDao.isPhoneNumberTaken(req.getPhone())) {
                String response = "{\"error\": \"Phone number already exists\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(409, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            Restaurant restaurant = new Restaurant(req, (Seller) user);
            RestaurantDao.saveRestaurant(restaurant, user.getUserId());
            
            String response = "Restaurant created successfully";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorResponse = "{\"error\": \"Internal server error\"}";
            byte[] responseBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }


    }

    private void handleGetSellersRestaurants(HttpExchange exchange, User user) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            List<RestaurantDto> restaurantsList = ((Seller) user).getDtoRestaurants();
            Gson gson = new Gson();
            String json = gson.toJson(restaurantsList);
            byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorResponse = "{\"error\": \"Internal server error\"}";
            byte[] responseBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

    }
}
