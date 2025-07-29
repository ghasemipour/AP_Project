package com.ap.project.httpHandler;

import com.ap.project.Enums.ApprovalStatus;
import com.ap.project.Enums.CouponType;
import com.ap.project.Exceptions.NoSuchCoupon;
import com.ap.project.Exceptions.NoSuchUser;
import com.ap.project.dao.CouponDao;
import com.ap.project.dao.OrderDao;
import com.ap.project.dao.TransactionDao;
import com.ap.project.dao.UserDao;
import com.ap.project.deserializer.CouponTypeDeserializer;
import com.ap.project.dto.CouponDto;
import com.ap.project.dto.OrderDto;
import com.ap.project.dto.ProfileDto;
import com.ap.project.dto.TransactionDto;
import com.ap.project.entity.general.Coupon;
import com.ap.project.entity.user.Admin;
import com.ap.project.entity.user.NeedApproval;
import com.ap.project.entity.user.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class AdminHttpHandler extends SuperHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = getUserByExchange(exchange);
        if (user == null) {
            return;
        }

        if(!(user instanceof Admin)){
            exchange.sendResponseHeaders(403,-1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        String method = exchange.getRequestMethod();

        if(parts.length == 3){
            if(parts[2].equals("users")){
                if(!method.equals("GET")){
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                handleGetListOfUsers(exchange);
            } else if(parts[2].equals("coupons")){
                if(method.equals("GET")){
                    handleGetListOfCoupons(exchange);

                } else if(method.equals("POST")){
                    handleCreatCoupon(exchange);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
            } else if (parts[2].equals("orders")) {
                if (!method.equals("GET")){
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                handleViewAllOrders(exchange);
            } else if (parts[2].equals("transactions")) {
                if (!method.equals("GET")) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                handleViewAllTransactions(exchange);
            }
        } else if (parts.length == 4){
            if(parts[2].equals("users")){
                if(!method.equals("DELETE")) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                handleDeleteUser(exchange, Integer.parseInt(parts[3]));
            } else {
                Coupon coupon = CouponDao.getCouponById(Integer.parseInt(parts[3]));
                if (coupon == null) {
                    exchange.sendResponseHeaders(404, -1);
                    throw new NoSuchCoupon(parts[3] + "Coupon not found");
                }
                if (method.equals("DELETE")) {
                    handleDeleteCoupon(exchange, coupon);
                } else if (method.equals("GET")) {
                    handleGetCouponDetails(exchange, coupon);

                } else if (method.equals("PUT")) {
                    handleUpdateCoupon(exchange, coupon);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        }else if(parts.length == 5){
            if(!method.equals("PATCH")){
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleChangeUserStatus(exchange, Integer.parseInt(parts[3]));
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }


    private void handleGetListOfUsers(HttpExchange exchange) throws IOException {
        try {
            List<ProfileDto> users = UserDao.getListOfUsers(exchange);
            sendSuccessMessage(new Gson().toJson(users), exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    private void handleChangeUserStatus(HttpExchange exchange, int userId) throws IOException {
        try {
            User user = UserDao.getUserById(userId);
            if(user == null){
                exchange.sendResponseHeaders(404, -1);
                throw new NoSuchUser(userId + "user not found");
            }
            if(! (user instanceof NeedApproval)){
                exchange.sendResponseHeaders(403,-1);
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
            if(!(json.has("status") || json.get("status").isJsonNull())){
                String response = "Status required.";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            ApprovalStatus status = ApprovalStatus.fromString(json.get("status").getAsString());
            if(status == null){
                String response = "wrong status";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            UserDao.ChangeUserStatus(userId, status);
            sendSuccessMessage("Status updated", exchange);
        } catch(Exception e){
            internalServerFailureError(e, exchange);
        }

    }

    private void handleDeleteUser(HttpExchange exchange, int userId) throws IOException {
        try {
            System.out.println(userId);
            UserDao.deleteUserById(userId, exchange);
            System.out.println("user deleted");
            sendSuccessMessage("User deleted", exchange);

        } catch (Exception e){
            internalServerFailureError(e, exchange);
        }
    }

    private void handleViewAllOrders(HttpExchange exchange) throws IOException {
        try {
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
            String courier = queryParams.get("courier");
            String customer = queryParams.get("customer");
            String status = queryParams.get("status");
            List<OrderDto> results = OrderDao.getAllOrders(search, vendor, courier, customer, status);
            sendSuccessMessage(new Gson().toJson(results), exchange);
        } catch (IllegalArgumentException | IllegalStateException e) {
            String response = "Invalid enum";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }


    private void handleViewAllTransactions(HttpExchange exchange) throws IOException {
        try {
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
            String user = queryParams.get("user");
            String method = queryParams.get("method");
            String status = queryParams.get("status");
            List<TransactionDto> result = TransactionDao.getAllTransactions(search, user, method, status);
            sendSuccessMessage(new Gson().toJson(result), exchange);

        } catch (IllegalArgumentException | IllegalStateException e) {
            String response = "Invalid enum";
            byte[] responseByte = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseByte.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseByte);
            }
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }


    private void handleCreatCoupon(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader sr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(CouponType.class, new CouponTypeDeserializer()).create();
            CouponDto req = gson.fromJson(sr, CouponDto.class);

            if (req.getType() == null || req.getCouponCode() == null || req.getMinPrice() == null || req.getValue() == null || req.getUserCount() == null || req.getStartDate() == null || req.getEndDate() == null) {
                String response = "";
                if (req.getCouponCode() == null)
                    response += "{\"error\": \"Coupon code required\"}\n";
                if (req.getMinPrice() == null)
                    response += "{\"error\": \"Min price required\"}\n";
                if (req.getValue() == null)
                    response += "{\"error\": \"Value required\"}\n";
                if (req.getUserCount() == null)
                    response += "{\"error\": \"User count required\"}\n";
                if (req.getStartDate() == null)
                    response += "{\"error\": \"Start date required\"}\n";
                if (req.getEndDate() == null)
                    response += "{\"error\": \"End date required\"}\n";
                if(req.getType() == null)
                    response += "{\"error\": \"Type required\"}\n";

                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            Coupon coupon = new Coupon(req.getType(), req.getCouponCode(), req.getMinPrice(), req.getValue(), req.getUserCount(), req.getStartDate(), req.getEndDate());
            if(CouponDao.isCouponCodeTaken(coupon.getCouponCode())) {
                String response = "{\"error\": \"Coupon code already exists\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(409, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            CouponDao.saveCoupon(coupon);
            sendSuccessMessage(new Gson().toJson(coupon.getCouponDto()), exchange);

        } catch (Exception e){
            internalServerFailureError(e, exchange);
        }
    }

    private void handleGetListOfCoupons(HttpExchange exchange) throws IOException {
        try{
            List<CouponDto> coupons = CouponDao.getListOfCoupons(exchange);
            sendSuccessMessage(new Gson().toJson(coupons), exchange);

        }catch (Exception e){
            internalServerFailureError(e, exchange);
        }
    }


    private void handleDeleteCoupon(HttpExchange exchange, Coupon coupon) throws IOException {
        try {
            CouponDao.deleteCouponById(coupon.getId());
            sendSuccessMessage("Coupon deleted", exchange);
        }catch (Exception e){
            internalServerFailureError(e, exchange);
        }
    }

    private void handleUpdateCoupon(HttpExchange exchange, Coupon coupon) throws IOException {
        try {
            InputStreamReader sr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(CouponType.class, new CouponTypeDeserializer()).create();
            CouponDto newCoupon = gson.fromJson(sr, CouponDto.class);
            if(CouponDao.isCouponCodeTaken(newCoupon.getCouponCode())) {
                String response = "{\"error\": \"Coupon code already exists\"}";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(409, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            CouponDao.updateCoupon(coupon.getId(), newCoupon);
            sendSuccessMessage(new Gson().toJson(coupon.getCouponDto()), exchange);
        }catch (Exception e){
            internalServerFailureError(e, exchange);
        }
    }

    private void handleGetCouponDetails(HttpExchange exchange, Coupon coupon) throws IOException {
        try {
            CouponDto couponDto = coupon.getCouponDto();
            sendSuccessMessage(new Gson().toJson(couponDto), exchange);
        } catch (Exception e){
            internalServerFailureError(e, exchange);
        }
    }
}
