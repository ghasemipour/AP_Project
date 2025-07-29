package com.ap.project.httpHandler;

import com.ap.project.Enums.ApprovalStatus;
import com.ap.project.Exceptions.NoSuchFoodItem;
import com.ap.project.dao.FoodItemDao;
import com.ap.project.dao.RestaurantDao;
import com.ap.project.dto.DiscountDto;
import com.ap.project.dto.FoodDto;
import com.ap.project.entity.restaurant.Food;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.Seller;
import com.ap.project.entity.user.User;
import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.ap.project.httpHandler.SuperHttpHandler.*;

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
            if (!(user instanceof Seller) || !(((Seller) user).getApprovalStatus().equals(ApprovalStatus.APPROVED))) {
                String response = "User not approved";
                exchange.sendResponseHeaders(403, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            Restaurant restaurant = RestaurantDao.getRestaurantById(Integer.parseInt(parts[2]));

            if (restaurant == null) {
                String response = "Restaurant not found";
                exchange.sendResponseHeaders(404, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            if (RestaurantDao.getSellerId(restaurant.getId()) != user.getUserId()) {
                String response = "Restaurant not owned by user";
                exchange.sendResponseHeaders(403, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            if (parts.length == 4) {
                if (method.equals("POST"))
                    handleAddFoodItem(exchange, restaurant);
                else if (method.equals("GET")) {
                    handleGetItems(exchange, restaurant);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } else if (parts.length == 5) {
                int foodID = Integer.parseInt(parts[4]);

                if (parts[3].equals("discount")) {
                    switch (method) {
                        case "POST":
                            handleAddDiscount(exchange, foodID, restaurant);
                            return;
                        case "DELETE":
                            handleDeleteDiscount(exchange, foodID, restaurant);
                            return;
                        default:
                            exchange.sendResponseHeaders(405, -1);
                    }
                }
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
        } else {
            exchange.sendResponseHeaders(404, -1);
            System.out.println("invalid request");
        }


    }

    public void handleGetItems(HttpExchange exchange, Restaurant restaurant) throws IOException {
        try {
            List<FoodDto> foodList = FoodItemDao.getItemsByRestaurantId(restaurant.getId(), exchange);
            sendSuccessMessage(new Gson().toJson(foodList), exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleAddFoodItem(HttpExchange exchange, Restaurant restaurant) throws IOException {
        try {
            InputStreamReader input = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            FoodDto req = new Gson().fromJson(input, FoodDto.class);

            String response = "";
            if (req.getName() == null)
                response += "{\"error\": \"Name required\"}\n";
            if (req.getDescription() == null)
                response += "{\"error\": \"Description required\"}\n";
            if (req.getPrice() == null || req.getPrice() <= 0)
                response += "{\"error\": \"Price required\"}\n";
            if (req.getSupply() == null || req.getSupply() <= 0)
                response += "{\"error\": \"Supply required\"}\n";
            if (req.getVendor_id() != restaurant.getId())
                response += "{\"error\": \"Vendor ID does not match\"}\n";


            if (!response.isEmpty()) {
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            Food food = new Food(req, restaurant);
            FoodItemDao.saveFood(food, restaurant.getId(), exchange);
            FoodDto dto = food.getFoodDto();
            sendSuccessMessage(new Gson().toJson(dto), exchange);

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
            food = FoodItemDao.getFoodByID(foodId, exchange);
            FoodDto dto = food.getFoodDto();
            sendSuccessMessage(new Gson().toJson(dto), exchange);
        } catch (Exception e) {
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

            int rating = json.has("rating") && !json.get("rating").isJsonNull()
                    ? json.get("rating").getAsInt()
                    : -1;

            List<String> keywords = new ArrayList<>();
            if (json.has("keywords") && json.get("keywords").isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray("keywords")) {
                    keywords.add(element.getAsString());
                }
            }
            List<FoodDto> results = FoodItemDao.getItemsByFilters(search, price, keywords, rating);
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

    private void handleAddDiscount(HttpExchange exchange, int foodId, Restaurant restaurant) throws IOException {
        try {
            Food food = FoodItemDao.getFoodByID(foodId, exchange);

            if (food == null || food.getRestaurant() == null || food.getRestaurant().getId() != restaurant.getId()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
            DiscountDto discountDto = new Gson().fromJson(reader, DiscountDto.class);


            if (discountDto.getPercentage() == null) {
                byte[] responseBytes = "{\"error\": \"percentage required\"}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            System.out.println(discountDto.getPercentage());
            if (discountDto.getPercentage() < 0 || discountDto.getPercentage() > 100) {
                byte[] responseBytes = "{\"error\": \"invalid percentage\"}".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            boolean discounted = FoodItemDao.saveDiscount(foodId, discountDto.getPercentage());

            if (discounted)
                sendSuccessMessage("food discounted successfully", exchange);
            else
                sendSuccessMessage("food was not discounted", exchange);

        } catch (NoSuchFoodItem | IllegalStateException e) {
            sendNotFoundMessage(e.getLocalizedMessage(), exchange);
        } catch (NumberFormatException e) {
            byte[] responseBytes = "{\"error\": \"invalid numeric error\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteDiscount(HttpExchange exchange, int foodId, Restaurant restaurant) throws IOException {
        try {
            Food food = FoodItemDao.getFoodByID(foodId, exchange);

            if (food == null || food.getRestaurant() == null || food.getRestaurant().getId() != restaurant.getId()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            if (food.getDiscountPercentage() == null || food.getDiscountPercentage() == 0) {
                String response = "{\"error\": \"food not discounted\"}";
                exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }

            FoodItemDao.deleteDiscount(foodId);
            sendSuccessMessage("discount removed successfully.", exchange);
        } catch (NoSuchFoodItem e) {
            sendNotFoundMessage(e.getLocalizedMessage(), exchange);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
