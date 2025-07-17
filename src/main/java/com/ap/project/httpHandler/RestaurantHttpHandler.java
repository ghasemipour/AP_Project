package com.ap.project.httpHandler;

import com.ap.project.Enums.Status;
import com.ap.project.Exceptions.NoSuchRestaurant;
import com.ap.project.dao.FoodItemDao;
import com.ap.project.dao.MenuDao;
import com.ap.project.dao.OrderDao;
import com.ap.project.dao.RestaurantDao;
import com.ap.project.dto.FoodDto;
import com.ap.project.dto.RestaurantDto;
import com.ap.project.entity.restaurant.Food;
import com.ap.project.entity.restaurant.Menu;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Courier;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.Seller;
import com.ap.project.entity.user.User;
import com.ap.project.services.Validate;
import com.ap.project.wrappers.StatusWrapper;
import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RestaurantHttpHandler extends SuperHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = getUserByExchange(exchange);
        if (user == null) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if(path.startsWith("/restaurants")){
            if (!(user instanceof Seller) || (!((Seller) user).getApprovalStatus().equals("APPROVED"))) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            if (path.equals("/restaurants")) {
                handleAddNewRestaurant(exchange, user);
            }else if (path.equals("/restaurants/mine")) {
                handleGetSellersRestaurants(exchange, user);
            } else {
                if (parts.length == 3) {
                    handleUpdateRestaurant(exchange, user, Integer.parseInt(parts[2]));
                } else if (parts.length >= 4) {
                    if (parts[3].equals("item")) {
                        FoodHttpHandler foodHandler = new FoodHttpHandler();
                        foodHandler.handle(exchange);
                    } else if (parts[3].equals("menu")) {
                        MenuHttpHandler menuHandler = new MenuHttpHandler();
                        menuHandler.handle(exchange);
                    } else if (parts[3].startsWith("orders")) {
                        handleGetRestaurantsOrders(exchange, Integer.parseInt(parts[2]), user);
                    } else if (parts[2].equals("orders")) {
                        handleChangeOrderStatus(exchange, Integer.parseInt(parts[3]), user);
                    }
                }
            }
        } else if(path.startsWith("/vendors")){
            if (!(user instanceof Customer)|| (!((Courier) user).getApprovalStatus().equals("APPROVED"))) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            if(path.equals("/vendors")){
                handleGetListOfRestaurants(exchange);
            } else if(parts.length == 3) {
                handleGetListOfMenus(exchange, Integer.parseInt(parts[2]));
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }

    }

    private void handleAddNewRestaurant(HttpExchange exchange, User user) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            RestaurantDto req = new Gson().fromJson(reader, RestaurantDto.class);

            if (req.getName() == null || req.getPhone() == null || req.getAddress() == null) {
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

            if (!Validate.validatePhone(req.getPhone(), exchange)) {
                return;
            }

            if (RestaurantDao.isPhoneNumberTaken(req.getPhone())) {
                String response = "{\"error\": \"Phone number already exists\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(409, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            Restaurant restaurant = new Restaurant(req, (Seller) user);
            RestaurantDao.saveRestaurant(restaurant, user.getUserId(), exchange);
            sendSuccessMessage("Restaurant created successfully", exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }


    }

    private void handleGetSellersRestaurants(HttpExchange exchange, User user) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            List<RestaurantDto> restaurantsList = ((Seller) user).getDtoRestaurants(exchange);
            sendSuccessMessage(new Gson().toJson(restaurantsList), exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }

    }

    private void handleUpdateRestaurant(HttpExchange exchange, User user, int restaurantId) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("PUT")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            RestaurantDto req = new Gson().fromJson(reader, RestaurantDto.class);
            Restaurant restaurant = RestaurantDao.getRestaurantById(restaurantId);
            if (restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            if (!(RestaurantDao.getSellerId(restaurantId) == user.getUserId())) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            if (req.getPhone() != null && !Validate.validatePhone(req.getPhone(), exchange)) {
                return;
            }

            if (req.getPhone() != null && RestaurantDao.isPhoneNumberTaken(req.getPhone())) {
                String response = "{\"error\": \"Phone number already exists\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            RestaurantDao.updateRestaurant(restaurantId, req);
            sendSuccessMessage("Restaurant updated successfully.", exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    private void handleGetRestaurantsOrders(HttpExchange exchange, int restaurantId, User seller) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            if (RestaurantDao.getSellerId(restaurantId) != seller.getUserId()) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            String query = exchange.getRequestURI().getQuery();
            HashMap<String, String> queryParams = new HashMap<>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                    String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                    queryParams.put(key, value);
                }
            }

            String status = queryParams.get("status");
            String search = queryParams.get("search");
            String user = queryParams.get("user");
            String courier = queryParams.get("courier");

            List<Order> orders = RestaurantDao.getRestaurantOrdersByRestaurantId(restaurantId, status, search, user, courier);
            sendSuccessMessage(new Gson().toJson(orders), exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    private void handleChangeOrderStatus(HttpExchange exchange, int orderId, User user) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("PATCH")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Order order = OrderDao.getOrderFromId(orderId, exchange);

            if (order == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            int restaurantId = order.getRestaurant().getId();

            if (RestaurantDao.getSellerId(restaurantId) != user.getUserId()) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            StatusWrapper statusWrp = new Gson().fromJson(reader, StatusWrapper.class);

            if (statusWrp == null || statusWrp.status == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            String statusStr = statusWrp.status.toString();
            Status statusEnum = Status.fromString(statusStr);
            if (statusEnum == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            OrderDao.changeOrderStatus(orderId, statusEnum, exchange);
            sendSuccessMessage("Status changed successfully.", exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    private void handleGetListOfRestaurants(HttpExchange exchange) throws IOException {
        try {
            if(!exchange.getRequestMethod().equals("POST")) {
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

            String search = (json.has("search") && !json.get("search").isJsonNull())
                    ? json.get("search").getAsString()
                    : null;

            List<String> keywords = new ArrayList<>();
            if(json.has("keywords") && !json.get("keywords").isJsonNull() && json.get("keywords").isJsonArray()) {
                for(JsonElement jsonElement : json.get("keywords").getAsJsonArray()) {
                    keywords.add(jsonElement.getAsString());
                }
            }

            List<RestaurantDto> results = RestaurantDao.getRestaurantsByFilter(search, keywords);
            sendSuccessMessage(new Gson().toJson(results), exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    private void handleGetListOfMenus(HttpExchange exchange, int restaurantId) throws IOException {

        try {
            if(!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Restaurant restaurant = RestaurantDao.getRestaurantById(restaurantId);
            if (restaurant == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject responseJson = new JsonObject();
            JsonArray menuTitles = new JsonArray();

            List<Menu> menus = RestaurantDao.getRestaurantMenus(restaurantId, exchange);
            if(menus == null ||menus.isEmpty()){
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            for(Menu menu : menus) {
                String title = menu.getTitle();
                menuTitles.add(title);

                JsonArray foodArray = new JsonArray();
                List<Food> foodItems = MenuDao.getFoodItems(menu.getId(), exchange);
                if(foodItems != null && !foodItems.isEmpty()){

                    for(Food food : foodItems){
                        System.out.println(food.getName());
                        JsonObject foodJson = new JsonObject();
                        foodJson.addProperty("name", food.getName());
                        foodJson.addProperty("price", food.getPrice());
                        foodJson.addProperty("description", food.getDescription());
                        foodJson.addProperty("id", food.getFoodId());
                        foodJson.addProperty("supply", food.getSupply());
                        System.out.println(foodJson);
                        JsonArray keywordsArray = new JsonArray();
                        List<String> keywords = FoodItemDao.getKeywords(food.getFoodId());
                        if(keywords != null && !keywords.isEmpty()){
                            for (String keyword : keywords) {
                                keywordsArray.add(keyword);
                            }
                        }
                        foodJson.add("keywords", keywordsArray);

                        if(food.getImageBase64() != null) {
                            foodJson.addProperty("image", food.getImageBase64());
                        }

                        foodArray.add(foodJson);

                    }
                }
                responseJson.add(title, foodArray);

            }

            responseJson.add("menuTitles", menuTitles);
            sendSuccessMessage(new Gson().toJson(responseJson), exchange);

        } catch (IOException e) {
            internalServerFailureError(e, exchange);
        }

    }
}
