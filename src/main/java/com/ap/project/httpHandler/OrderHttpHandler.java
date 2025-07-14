package com.ap.project.httpHandler;

import com.ap.project.dao.OrderDao;
import com.ap.project.dto.OrderDto;
import com.ap.project.entity.restaurant.Order;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.User;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import static com.ap.project.httpHandler.SuperHttpHandler.*;

public class OrderHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = getUserByExchange(exchange);

        if (user == null) {
            return;
        }

        if (!(user instanceof Customer)) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (path.startsWith("/orders")) {
            if (path.equals("/orders")) {
                handleSubmitOrder(exchange, user);
            } else {
                if (path.equals("/orders/history")) {
                    handleGetOrderHistory(exchange, user);
                } else {
                    handleGetDetailsOfOrder(exchange, Integer.parseInt(parts[2]));
                }
            }
        }
    }

    public void handleSubmitOrder(HttpExchange exchange, User user) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405,-1);
                return;
            }

            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            OrderDto orderDto = new Gson().fromJson(reader, OrderDto.class);

            String response = "";
            if (orderDto.getDelivery_address() == null)
                response += "{\"error\": \"Address required\"}\n";
            if (orderDto.getVendor_id() == null)
                response += "{\"error\": \"Vendor ID required\"}\n";
            if (orderDto.getItems() == null) // check this later
                response += "{\"error\": \"Please select your items.\"}\n";
            if (!response.isEmpty()) {
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            Order order = new Order(orderDto, exchange, (Customer) user);
            OrderDao.submitOrder(order, orderDto.getVendor_id(), user.getUserId(), exchange);
            sendSuccessMessage("Order submitted successfully.", exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleGetDetailsOfOrder(HttpExchange exchange, int orderId) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Order order = OrderDao.getOrderFromId(orderId, exchange);
            OrderDto orderDto = order.getOrderDto();

            sendSuccessMessage(new Gson().toJson(orderDto), exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleGetOrderHistory(HttpExchange exchange, User user) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
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

            String search = queryParams.get("search");
            String vendor = queryParams.get("vendor");

            List<OrderDto> results = OrderDao.getOrderHistory(user.getUserId(), search, vendor);
            if (results.isEmpty()) {
                String response = "No order history found.";
                sendSuccessMessage(response, exchange);
                return;
            }
            sendSuccessMessage(new Gson().toJson(results), exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }
}
