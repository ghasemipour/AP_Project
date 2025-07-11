package com.ap.project.httpHandler;

import com.ap.project.dao.FoodItemDao;
import com.ap.project.dao.RestaurantDao;
import com.ap.project.dto.FoodDto;
import com.ap.project.dto.RestaurantDto;
import com.ap.project.entity.restaurant.Food;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.Seller;
import com.ap.project.entity.user.User;
import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Hibernate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.ap.project.httpHandler.SuperHttpHandler.internalServerFailureError;
import static com.ap.project.httpHandler.SuperHttpHandler.sendSuccessMessage;

public class FoodHttpHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        String method = exchange.getRequestMethod();
        User user = SuperHttpHandler.getUserByExchange(exchange);

        if (user == null) {
            return;
        }

        if (path.startsWith("/restaurant")) {
            Restaurant restaurant = RestaurantDao.getRestaurantById(Integer.parseInt(parts[2]));
            if (restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                System.out.println("Restaurant not found");
                return;
            }

            if (RestaurantDao.getSellerId(restaurant.getId()) != user.getUserId()) {
                exchange.sendResponseHeaders(403, -1);
                System.out.println("Restaurant not owned by user");
                return;
            }

            if (parts.length == 4) {
                if (method.equals("POST"))
                    handleAddFoodItem(exchange, restaurant);
                else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } else if (parts.length == 5) {

                int foodID = Integer.parseInt(parts[4]);

                switch (method) {
                    case "PUT":
                        handleEditFoodItem(exchange, foodID, restaurant);
                        return;
                    case "DELETE":
                        handleDeleteFoodItem(exchange, foodID, restaurant);
                        return;
                    default:
                        exchange.sendResponseHeaders(405, -1);
                }
            }
        } else if (path.startsWith("/items")) {
            if (!(user instanceof Customer)) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            if (parts.length == 2)
                handleGetListOfItems(exchange);
            else
                handleGetItemDetails(exchange, Integer.parseInt(parts[2]));
        }
        else {
            exchange.sendResponseHeaders(404, -1);
            System.out.println("invalid request");
        }


    }

    public void handleAddFoodItem(HttpExchange exchange, Restaurant restaurant) throws IOException {
        try {
            InputStreamReader input = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            FoodDto req = new Gson().fromJson(input, FoodDto.class);
            if (req.getName() == null || req.getDescription() == null || req.getPrice() == null || req.getSupply() == null || req.getVendor_id() != restaurant.getId()) {
                String response = "";
                if (req.getName() == null)
                    response += "{\"error\": \"Name required\"}\n";
                if (req.getDescription() == null)
                    response += "{\"error\": \"Description required\"}\n";
                if (req.getPrice() == null)
                    response += "{\"error\": \"Price required\"}\n";
                if (req.getSupply() == null)
                    response += "{\"error\": \"Supply required\"}\n";
                if (req.getVendor_id() != restaurant.getId())
                    response += "{\"error\": \"Vendor ID does not match\"}\n";

                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            Food food = new Food(req, restaurant);
            FoodItemDao.saveFood(food, restaurant.getId(), exchange);
            sendSuccessMessage("Food item created successfully.", exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleEditFoodItem(HttpExchange exchange, int foodId, Restaurant restaurant) throws IOException {
        try {
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            FoodDto req = new Gson().fromJson(reader, FoodDto.class);

            Food food = FoodItemDao.getFoodByID(foodId, exchange);

            if (food == null || food.getRestaurant() == null || food.getRestaurant().getId() != restaurant.getId()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            FoodItemDao.updateFood(req, foodId, exchange);
            sendSuccessMessage("Food item updated successfully.", exchange);
        }
        catch (Exception e) {
            internalServerFailureError(e, exchange);
        }

    }

    public void handleDeleteFoodItem(HttpExchange exchange, int foodId, Restaurant restaurant) throws IOException {
        try {
            Food food = FoodItemDao.getFoodByID(foodId, exchange);
            if (food == null || food.getRestaurant() == null || food.getRestaurant().getId() != restaurant.getId()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            FoodItemDao.deleteFood(foodId, exchange);
            sendSuccessMessage("Food successfully removed.", exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleGetListOfItems(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString();
            JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

            String search = json.has("search") && !json.get("search").isJsonNull()
                    ? json.get("search").getAsString()
                    : null;

            int price = json.has("price") && !json.get("price").isJsonNull()
                    ? json.get("price").getAsInt()
                    : -1;

            List<String> keywords = new ArrayList<>();
            if (json.has("keywords") && json.get("keywords").isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray("keywords")) {
                    keywords.add(element.getAsString());
                }
            }

            List<FoodDto> results = FoodItemDao.getItemsByFilters(search, price, keywords);
            sendSuccessMessage(new Gson().toJson(results), exchange);


        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleGetItemDetails(HttpExchange exchange, int foodId) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Food food = FoodItemDao.getFoodByID(foodId, exchange);
            FoodDto foodDto = food.getFoodDto();
            sendSuccessMessage(new Gson().toJson(foodDto), exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }
}
