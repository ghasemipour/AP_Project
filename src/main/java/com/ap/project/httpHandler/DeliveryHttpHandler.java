package com.ap.project.httpHandler;

import com.ap.project.Enums.Status;
import com.ap.project.dao.OrderDao;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.restaurant.OrderItem;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Courier;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
}
