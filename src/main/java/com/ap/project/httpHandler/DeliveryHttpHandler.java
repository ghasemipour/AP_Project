package com.ap.project.httpHandler;

import com.ap.project.Enums.Status;
import com.ap.project.dao.OrderDao;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.restaurant.OrderItem;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Courier;
import com.ap.project.entity.user.Customer;
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
import java.util.concurrent.ExecutionException;

public class DeliveryHttpHandler extends SuperHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = getUserByExchange(exchange);
        if (user == null) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        String method = exchange.getRequestMethod();

        if(path.startsWith("/deliveries")){
            if(!(user instanceof Courier)) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            if(parts[2].equals("available")){
                if(!(method.equals("GET"))){
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                handleGetAvailableDeliveryRequests(exchange, (Courier) user);
            } else if(parts[2].equals("history")){
                if(!(method.equals("GET"))){
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                handleGetDeliveryHistory(exchange, (Courier) user);

            } else{
                if(!(method.equals("PATCH"))){
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                handleChangeOrderStatus(exchange, (Courier) user, Integer.parseInt(parts[2]));

            }
        }
    }

    private void handleGetAvailableDeliveryRequests(HttpExchange exchange, Courier courier) throws IOException {
        try {
            List<Order> availableDeliveries = OrderDao.getOrderByStatus(exchange, Status.FINDING_COURIER);
            JsonArray deliveryArray = new JsonArray();
            for (Order order : availableDeliveries) {
                JsonObject delivery = new JsonObject();
                delivery.addProperty("id", order.getId());
                delivery.addProperty("delivery_address", order.getDelivery_address());
                JsonObject buyer = new JsonObject();
                Customer customer = OrderDao.getCustomer(order.getId(), exchange);
                buyer.addProperty("buyer name", customer.getName());
                buyer.addProperty("buyer phone", customer.getPhoneNumber());
                delivery.add("buyer", buyer);
                JsonObject restaurantJson = new JsonObject();
                Restaurant restaurant = OrderDao.getRestaurant(order.getId(), exchange);
                restaurantJson.addProperty("restaurant name", restaurant.getName());
                restaurantJson.addProperty("restaurant address", restaurant.getAddress());
                restaurantJson.addProperty("restaurant phone", restaurant.getPhone());
                delivery.add("restaurant", restaurantJson);
                deliveryArray.add(delivery);
            }
            JsonObject responseJson = new JsonObject();
            responseJson.add("available deliveries", deliveryArray);
            sendSuccessMessage(new Gson().toJson(responseJson), exchange);
        } catch (IOException e) {
            internalServerFailureError(e, exchange);
        }

    }

    private void handleChangeOrderStatus(HttpExchange exchange, Courier courier, int orderId) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString();
            JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
            if(!json.has("status") || json.get("status").isJsonNull()) {
                String response = "new status required";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            Status status = Status.fromString(json.get("status").getAsString());
            if(status == null){
                String response = "wrong status";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            if(status.equals(Status.ACCEPTED)){
                status = Status.Courier_Accepted;
            }
            OrderDao.changeOrderStatus(orderId, status, exchange);
            sendSuccessMessage("Changed status successfully", exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }

    }

    private void handleGetDeliveryHistory(HttpExchange exchange, Courier courier) throws IOException {
        try {
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

            String vendorId = (json.has("vendor") && !json.get("vendor").isJsonNull())
                    ? json.get("vendor").getAsString()
                    : null;

            String userId = (json.has("user") && !json.get("user").isJsonNull())
                    ? json.get("user").getAsString()
                    : null;

            List<Order> orders = OrderDao.getDeliveryHistory(courier, search, vendorId, userId);
            JsonArray deliveryArray = new JsonArray();
            for (Order order : orders) {
                JsonObject delivery = new JsonObject();
                delivery.addProperty("id", order.getId());
                delivery.addProperty("delivery_address", order.getDelivery_address());
                JsonObject buyer = new JsonObject();
                Customer customer = OrderDao.getCustomer(order.getId(), exchange);
                buyer.addProperty("buyer name", customer.getName());
                buyer.addProperty("buyer phone", customer.getPhoneNumber());
                delivery.add("buyer", buyer);
                JsonObject restaurantJson = new JsonObject();
                Restaurant restaurant = OrderDao.getRestaurant(order.getId(), exchange);
                restaurantJson.addProperty("restaurant name", restaurant.getName());
                restaurantJson.addProperty("restaurant address", restaurant.getAddress());
                restaurantJson.addProperty("restaurant phone", restaurant.getPhone());
                delivery.add("restaurant", restaurantJson);
                deliveryArray.add(delivery);
            }
            JsonObject responseJson = new JsonObject();
            responseJson.add("deliveries history", deliveryArray);
            sendSuccessMessage(new Gson().toJson(responseJson), exchange);
        } catch (Exception e){
            internalServerFailureError(e, exchange);
        }
    }
}
