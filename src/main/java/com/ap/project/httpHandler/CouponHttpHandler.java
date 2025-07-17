package com.ap.project.httpHandler;

import com.ap.project.dao.CouponDao;
import com.ap.project.dto.CouponDto;
import com.ap.project.entity.general.Coupon;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class CouponHttpHandler extends SuperHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            User user = getUserByExchange(exchange);
            if (user == null) {
                return;
            }
            if(!(user instanceof Customer)){
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            if(!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString();
            JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
            if(!json.has(" coupon_code") || json.get(" coupon_code").isJsonNull()){
                String response = "coupon code required";
                byte[] responseBytes = response.getBytes("UTF-8");
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            String couponCode = json.get(" coupon_code").getAsString();
            Coupon coupon = CouponDao.getCouponByCouponCode(couponCode);
            if(coupon == null){
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            CouponDto couponDto = coupon.getCouponDto();
            sendSuccessMessage(new Gson().toJson(couponDto), exchange);
        } catch (IOException e) {
            internalServerFailureError(e, exchange);
        }

    }
}
