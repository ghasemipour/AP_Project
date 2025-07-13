package com.ap.project.httpHandler;

import com.ap.project.dao.RatingDao;
import com.ap.project.dto.RatingDto;
import com.ap.project.entity.restaurant.Food;
import com.ap.project.entity.restaurant.Rating;
import com.ap.project.entity.user.Customer;
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

public class RatingHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = getUserByExchange(exchange);

        if (user == null)
            return;

        if (!(user instanceof Customer)) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (path.equals("/ratings")) {
            handleSubmitRating(exchange);
        } else {
            if (parts.length == 4) {
                handleGetRatings(exchange, Integer.parseInt(parts[3]));
            }
            else {
                String method = exchange.getRequestMethod();
                int ratingId = Integer.parseInt(parts[2]);
                switch (method) {
                    case "GET":
                        handleGetSpecificRating(exchange, ratingId);
                        return;
                    case "DELETE":
                        handleDeleteSpecificRating(exchange, ratingId);
                        return;
                    case "PUT":
                        handleUpdateSpecificRating(exchange, ratingId);
                        return;
                    default:
                        exchange.sendResponseHeaders(405, -1);
                }
            }
        }
    }

    public void handleSubmitRating(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            RatingDto req = new Gson().fromJson(reader, RatingDto.class);

            StringBuilder response = new StringBuilder();

            if (req.getOrder_id() == null)
                response.append("{\"error\": \"Order ID required\"}\n");
            if (req.getRating() == null)
                response.append("{\"error\": \"Rating number required\"}\n");
            if (req.getComment() == null)
                response.append("{\"error\": \"comment required\"}\n");
            if (!response.isEmpty()) {
                byte[] responseBytes = response.toString().getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }

            Rating rating = new Rating(req, exchange);
            RatingDao.submitRating(rating, exchange, req.getOrder_id(), req.getUserId());
            sendSuccessMessage("Rating submitted successfully.", exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleGetRatings(HttpExchange exchange, int itemId) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            List<RatingDto> result = RatingDao.getRatingsForItem(itemId);
            if (result.isEmpty()) {
                String response = "No ratings found.";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            sendSuccessMessage(new Gson().toJson(result), exchange);
            
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleGetSpecificRating(HttpExchange exchange, int ratingId) throws IOException {
        try {
            Rating rating = RatingDao.getRatingByID(ratingId, exchange);
            RatingDto result = rating.getRatingDto();
            sendSuccessMessage(new Gson().toJson(result), exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleDeleteSpecificRating(HttpExchange exchange, int ratingId) throws IOException {
        try {
            RatingDao.deleteRating(ratingId, exchange);
            sendSuccessMessage("Rating deleted successfully.", exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    public void handleUpdateSpecificRating(HttpExchange exchange, int ratingId) throws IOException {
        try {
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            RatingDto req = new Gson().fromJson(reader, RatingDto.class);
            RatingDao.updateRating(ratingId, req, exchange);
            sendSuccessMessage("Rating updated successfully.", exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }
}
