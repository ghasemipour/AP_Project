package com.ap.project.httpHandler;

import com.ap.project.Exceptions.NoSuchRestaurant;
import com.ap.project.dao.RestaurantDao;
import com.ap.project.dao.UserDao;
import com.ap.project.dto.RestaurantDto;
import com.ap.project.entity.restaurant.Restaurant;
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
        
        if(path.startsWith("/favorite")){
            if(!(user instanceof Customer)){
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            if(parts.length == 2){
                if(!method.equals("GET")){
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                handleGetFavoriteRestaurants(exchange, user);
            } else if(parts.length == 3){
                if(method.equals("PUT")){
                    handleAddRestaurantToFavorites(exchange, user, Integer.parseInt(parts[2]));
                } else if(method.equals("DELETE")){
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
            for(Restaurant restaurant : favorites){
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("name", restaurant.getName());
//                jsonObject.addProperty("id", restaurant.getId());
//                jsonObject.addProperty("address", restaurant.getAddress());
//                jsonObject.addProperty("phone", restaurant.getPhone());
//                jsonObject.addProperty("tax_fee", restaurant.getTax_fee());
//                jsonObject.addProperty("additional_fee", restaurant.getAdditional_fee());
//                if(restaurant.getLogoBase64() != null) {
//                    jsonObject.addProperty("logo", restaurant.getLogoBase64());
//                }
//                restaurants.add(jsonObject);
                res.add(restaurant.GetDto());
            }

//            JsonObject response = new JsonObject();
//            response.add("restaurants", restaurants);
            sendSuccessMessage(new Gson().toJson(res), exchange);

        } catch (Exception e) {
            internalServerFailureError(e, exchange);
        }
    }
}
