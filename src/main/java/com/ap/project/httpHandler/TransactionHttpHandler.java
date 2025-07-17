package com.ap.project.httpHandler;

import com.ap.project.Enums.TransactionMethod;
import com.ap.project.Enums.TransactionStatus;
import com.ap.project.dao.OrderDao;
import com.ap.project.dao.TransactionDao;
import com.ap.project.dto.TransactionDto;
import com.ap.project.entity.general.Transaction;
import com.ap.project.entity.general.Wallet;
import com.ap.project.entity.restaurant.Order;
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
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.ap.project.dao.UserDao.getWalletByUserId;
import static com.ap.project.httpHandler.SuperHttpHandler.*;

public class  TransactionHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = getUserByExchange(exchange);
        String path = exchange.getRequestURI().getPath();

        if (user == null)
            return;

        if(!(user instanceof Customer)){
            exchange.sendResponseHeaders(403,-1);
            return;
        }

        if (path.equals("/transactions")) {
            handleGetTransactionList(exchange, user.getUserId());
        } else if(path.equals("/wallet/top-up")){
            handleTopUpWallet(exchange, (Customer) user);
        }
        else if (path.equals("/payment/online")) {
            handleOnlinePayment(exchange, (Customer) user);
        }

    }

    private void handleGetTransactionList(HttpExchange exchange, int userId) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            List<TransactionDto> results = TransactionDao.getTransactionHistoryByUserID(userId);
            if (results == null || results.isEmpty())  {
                String response = "No transactions found.";
                sendSuccessMessage(response, exchange);
                return;
            }

            sendSuccessMessage(new Gson().toJson(results), exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    private void handleOnlinePayment(HttpExchange exchange, Customer user) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                requestBody.append(line);
            }
            JsonObject req = JsonParser.parseString(requestBody.toString()).getAsJsonObject();

            String response = "";
            if (!req.has("order_id")) {
                response += "{\"error\": \"Order ID required\"}\n";
            }

            if (!req.has("method")) {
                response += "{\"error\": \"Method of payment required\"}\n";
            }

            if (!response.isEmpty()) {
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            int orderId = req.get("order_id").getAsInt();
            String method = req.get("method").getAsString();

            if (!method.equals("online") && !method.equals("wallet")) {
                response = "{\"error\": \"Invalid payment method\"}\n";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            Order order = OrderDao.getOrderFromId(orderId, exchange);
            Wallet wallet = getWalletByUserId(user.getUserId(), exchange);
            if(wallet == null){
                user.setWallet(new Wallet());
                wallet = getWalletByUserId(user.getUserId(), exchange);
            }
            Transaction transaction = new Transaction(order, wallet, user, TransactionMethod.fromString(method), TransactionStatus.fromString("success"));
            boolean paymentStatus = TransactionDao.onlinePayment(transaction);
            TransactionDao.saveTransaction(transaction, user.getUserId(), orderId, wallet.getId(), exchange);
            if (!paymentStatus) {
                String error = "{\"error\": \"Balance not enough.\"}";
                byte[] responseBytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(403, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            sendSuccessMessage("Online payment successful.", exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    private void handleTopUpWallet(HttpExchange exchange, Customer customer) throws IOException {
        try {
            if(!exchange.getRequestMethod().equals("POST")){
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
            if(!json.has("amount") || json.get("amount").isJsonNull()){
                String response = "Amount required.";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            double amount = json.get("amount").getAsDouble();
            if(amount <= 0){
                String response = "Amount must be greater than 0.";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            Wallet wallet = getWalletByUserId(customer.getUserId(), exchange);
            if(wallet == null){
                customer.setWallet(new Wallet());
                wallet = getWalletByUserId(customer.getUserId(), exchange);
            }
            TransactionDao.topUpWallet(customer.getUserId(), amount, exchange);
            Transaction transaction = new Transaction(null, wallet, customer, TransactionMethod.WALLET, TransactionStatus.SUCCESS);
            TransactionDao.saveTransaction(transaction,customer.getUserId(), -1, wallet.getId(), exchange);
            sendSuccessMessage("Wallet topped up successfully", exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }
}
