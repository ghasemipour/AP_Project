package com.ap.project.httpHandler;

import com.ap.project.dao.UserDao;
import com.ap.project.dto.FoodDto;
import com.ap.project.dto.RestaurantDto;
import com.ap.project.entity.restaurant.Food;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Customer;
import com.ap.project.entity.user.User;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Hibernate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FavoriteHttpHandler extends SuperHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user = getUserByExchange(exchange);
        if (user == null) {
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        String method = exchange.getRequestMethod();

        if (path.startsWith("/favorites")) {
            if (!(user instanceof Customer)) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            if (parts.length == 2) {
                if (!method.equals("GET")) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                handleGetFavoriteRestaurants(exchange, user);
            } else if (parts.length == 3) {
                if (path.equals("/favorites/recommendations")) {
                    handleGetRecommendations(exchange, user);
                }
                    if (method.equals("PUT")) {
                        handleAddRestaurantToFavorites(exchange, user, Integer.parseInt(parts[2]));
                    } else if (method.equals("DELETE")) {
                        handleDeleteRestaurantFromFavorites(exchange, user, Integer.parseInt(parts[2]));

                    } else {
                        exchange.sendResponseHeaders(405, -1);
                    }

            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }

    private void handleAddRestaurantToFavorites(HttpExchange exchange, User user, int restaurantId) throws IOException {
        try {
            UserDao.addRestaurantToFavorites(user.getUserId(), restaurantId, exchange);
            sendSuccessMessage("Added to favorites", exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    private void handleDeleteRestaurantFromFavorites(HttpExchange exchange, User user, int restaurantId) throws IOException {
        try {
            UserDao.deleteRestaurantFromFavorites(user.getUserId(), restaurantId, exchange);
            sendSuccessMessage("Removed from favorites", exchange);
        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    private void handleGetFavoriteRestaurants(HttpExchange exchange, User user) throws IOException {
        try {
            List<Restaurant> favorites = UserDao.getFavoriteRestaurants(user.getUserId(), exchange);
            List<RestaurantDto> res = new ArrayList<RestaurantDto>();
            for (Restaurant restaurant : favorites) {
                res.add(restaurant.GetDto());
            }
            sendSuccessMessage(new Gson().toJson(res), exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }

    private void handleGetRecommendations(HttpExchange exchange, User user) {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            System.out.println("SENT");
            List<Restaurant> favorites = UserDao.getFavoriteRestaurants(user.getUserId(), exchange);

            List<FoodDto> results = UserDao.getRecommendations(favorites);

            sendSuccessMessage(new Gson().toJson(results), exchange);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
