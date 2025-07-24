package com.ap.project.httpHandler;

import com.ap.project.Enums.ApprovalStatus;
import com.ap.project.Enums.Status;
import com.ap.project.dao.OrderDao;
import com.ap.project.dto.DeliveryDto;
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
            if(!(user instanceof Courier) || (!((Courier) user).getApprovalStatus().equals(ApprovalStatus.APPROVED))) {
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

            } else if(parts[2].equals("mine")){
                if(!(method.equals("GET"))){
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                handleGetCurrentDelivery(exchange, (Courier) user);
            }else{
                if(!(method.equals("PATCH"))){
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                handleChangeOrderStatus(exchange, (Courier) user, Integer.parseInt(parts[2]));

            }
        }
    }

    private void handleGetCurrentDelivery(HttpExchange exchange, Courier user) throws IOException {
        try {
            List<DeliveryDto> deliveries = OrderDao.getOrderFromCourierId(user.getUserId(), exchange);
            sendSuccessMessage(new Gson().toJson(deliveries), exchange);

        } catch (Exception e){
            internalServerFailureError(e, exchange);
        }
    }

    private void handleGetAvailableDeliveryRequests(HttpExchange exchange, Courier courier) throws IOException {
        try {
            List<Order> availableDeliveries = OrderDao.getOrderByStatus(exchange, Status.FINDING_COURIER);
            List<DeliveryDto> res = new ArrayList<>();
            for (Order order : availableDeliveries) {

                res.add(order.getDeliveryDto(exchange));
            }
            sendSuccessMessage(new Gson().toJson(res), exchange);
        } catch (Exception e) {
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
                OrderDao.setOrderCourier(orderId, courier.getUserId(), exchange);
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
            System.out.println(requestBody);
            JsonElement element = JsonParser.parseString(requestBody);
            String search = null;
            String vendorId = null;
            String userId = null;
            if(element != null && element.isJsonObject()) {

                JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();

                search = (json.has("search") && !json.get("search").isJsonNull())
                        ? json.get("search").getAsString()
                        : null;

                vendorId = (json.has("vendor") && !json.get("vendor").isJsonNull())
                        ? json.get("vendor").getAsString()
                        : null;

                userId = (json.has("user") && !json.get("user").isJsonNull())
                        ? json.get("user").getAsString()
                        : null;
            }
            List<Order> orders = OrderDao.getDeliveryHistory(courier, search, vendorId, userId);
            List<DeliveryDto> deliveryArray = new ArrayList<>();
            for (Order order : orders) {
                deliveryArray.add(order.getDeliveryDto(exchange));
            }
            sendSuccessMessage(new Gson().toJson(deliveryArray), exchange);
        } catch (Exception e){
            internalServerFailureError(e, exchange);
        }
    }
}
