package com.ap.project.httpHandler;

import com.ap.project.dao.TransactionDao;
import com.ap.project.dto.TransactionDto;
import com.ap.project.entity.user.User;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.ap.project.httpHandler.SuperHttpHandler.*;

public class TransactionHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = getUserByExchange(exchange);
        String path = exchange.getRequestURI().getPath();

        if (user == null)
            return;

        if (path.equals("/transactions")) {
            handleGetTransactionList(exchange, user.getUserId());
        }
        else if (path.equals("/payment/online")) {

        }

    }

    public void handleGetTransactionList(HttpExchange exchange, int userId) throws IOException {
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

    public void handleOnlinePayment(HttpExchange exchange, int userId) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            TransactionDto req = new Gson().fromJson(reader, TransactionDto.class);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }
}
