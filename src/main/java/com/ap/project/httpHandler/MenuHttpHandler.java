package com.ap.project.httpHandler;

import com.ap.project.dao.FoodItemDao;
import com.ap.project.dao.MenuDao;
import com.ap.project.dao.RestaurantDao;
import com.ap.project.dto.MenuDto;
import com.ap.project.entity.restaurant.Food;
import com.ap.project.entity.restaurant.Menu;
import com.ap.project.entity.restaurant.Restaurant;
import com.ap.project.entity.user.Seller;
import com.ap.project.entity.user.User;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static com.ap.project.httpHandler.SuperHttpHandler.internalServerFailureError;
import static com.ap.project.httpHandler.SuperHttpHandler.sendSuccessMessage;

//TODO : check whether menu name is unique or not

public class MenuHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        Restaurant restaurant = RestaurantDao.getRestaurantById(Integer.parseInt(parts[2]));
        String method = exchange.getRequestMethod();
        User user = SuperHttpHandler.getUserByExchange(exchange);

        if (user == null) {
            return;
        }

        if (!(user instanceof Seller)|| (!((Seller) user).getApprovalStatus().equals("APPROVED"))) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        if (restaurant == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        if (!(RestaurantDao.getSellerId(restaurant.getId()) == user.getUserId())) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        if(parts.length == 4) {
            if (!method.equals("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleAddMenu(exchange, restaurant);
        } else if(parts.length == 5) {
            if(method.equals("DELETE")) {
                handleDeleteMenu(exchange, restaurant, parts[4]);
            } else if(method.equals("PUT")){
                handleAddFoodItemToMenu(exchange, restaurant, parts[4]);
            } else{
                exchange.sendResponseHeaders(405, -1);
                return;
            }
        } else if(parts.length == 6){
            if(method.equals("DELETE")) {
                handleDeleteFoodItemFromMenu(exchange, restaurant, parts[4], Integer.parseInt(parts[5]));
            }else {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
        }
    }



    private void handleAddMenu(HttpExchange exchange, Restaurant restaurant) throws IOException {
        try {
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
            MenuDto menuDto = new Gson().fromJson(reader, MenuDto.class);
            
            if(menuDto.getTitle() == null || menuDto.getTitle().isEmpty()) {
                String response = "title required";
                byte[] responseBytes = response.getBytes();
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            Menu menu = new Menu(menuDto);
            MenuDao.save(menu, restaurant.getId(), exchange);
            sendSuccessMessage("Food menu created and added to restaurant successfully", exchange);
            
        } catch (Exception e) {
            e.printStackTrace();
            internalServerFailureError(e, exchange);
            
        }
        
    }

    private void handleDeleteMenu(HttpExchange exchange, Restaurant restaurant, String menuTitle) throws IOException{
        try {
            Menu menu = MenuDao.getMenuByTitle(restaurant.getId(), menuTitle, exchange);
            if(menu == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            MenuDao.deleteMenu(restaurant.getId(), menu.getId(), exchange);
            sendSuccessMessage("Food menu removed from restaurant successfully", exchange);
        } catch (IOException e) {
            e.printStackTrace();
            internalServerFailureError(e, exchange);
        }

    }

    private void handleAddFoodItemToMenu(HttpExchange exchange, Restaurant restaurant, String menuTitle) throws IOException {
        try {
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
            Menu menu = MenuDao.getMenuByTitle(restaurant.getId(), menuTitle, exchange);
            if(menu == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            MenuDto menuDto = new Gson().fromJson(reader, MenuDto.class);
            if(menuDto.getItem_id() == null) {
                String response = "item_id required";
                byte[] responseBytes = response.getBytes();
                exchange.sendResponseHeaders(400, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            MenuDao.addFoodItem(menu.getId(), menuDto.getItem_id(), exchange);
            sendSuccessMessage("Food item created and added to restaurant successfully", exchange);

        } catch (IOException e) {
            e.printStackTrace();
            internalServerFailureError(e, exchange);
        }
    }

    private void handleDeleteFoodItemFromMenu(HttpExchange exchange, Restaurant restaurant, String menuTitle, int foodItemId) throws IOException {
        try {
            Menu menu = MenuDao.getMenuByTitle(restaurant.getId(), menuTitle, exchange);
            if(menu == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            MenuDao.deleteFoodItemFromMenu(menu.getId(), foodItemId, exchange);
            sendSuccessMessage("Item removed from restaurant menu successfully", exchange);
        } catch (IOException e) {
            e.printStackTrace();
            internalServerFailureError(e, exchange);
        }

    }
}
