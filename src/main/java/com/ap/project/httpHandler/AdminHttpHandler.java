package com.ap.project.httpHandler;

import com.ap.project.Enums.ApprovalStatus;
import com.ap.project.Enums.CouponType;
import com.ap.project.Enums.Status;
import com.ap.project.Enums.UserRole;
import com.ap.project.Exceptions.NoSuchUser;
import com.ap.project.dao.CouponDao;
import com.ap.project.dao.UserDao;
import com.ap.project.deserializer.CouponTypeDeserializer;
import com.ap.project.deserializer.UserRoleDeserializer;
import com.ap.project.dto.CouponDto;
import com.ap.project.dto.ProfileDto;
import com.ap.project.dto.RegisterDto;
import com.ap.project.entity.general.Coupon;
import com.ap.project.entity.user.Admin;
import com.ap.project.entity.user.Customer;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.ap.project.dao.UserDao.getUserById;

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
            }
        } else if(parts.length == 5){
            if(!method.equals("PATCH")){
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleChangeUserStatus(exchange, Integer.parseInt(parts[3]));
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
            CouponDao.saveCoupon(coupon);

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
}
