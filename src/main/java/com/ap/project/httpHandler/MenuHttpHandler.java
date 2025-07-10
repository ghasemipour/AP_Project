package com.ap.project.httpHandler;

import com.ap.project.dao.MenuDao;
import com.ap.project.dao.RestaurantDao;
import com.ap.project.dto.MenuDto;
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

        if (!(user instanceof Seller)) {
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

            } else{
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
            MenuDao.save(menu, restaurant.getId());
            sendSuccessMessage("Food menu created and added to restaurant successfully", exchange);
            
        } catch (Exception e) {
            e.printStackTrace();
            internalServerFailureError(e, exchange);
            
        }
        
    }

    private void handleDeleteMenu(HttpExchange exchange, Restaurant restaurant, String menuTitle) throws IOException{
        try {
            Menu menu = MenuDao.getMenuByTitle(restaurant.getId(), menuTitle);
            if(menu == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            MenuDao.deleteMenu(restaurant.getId(), menu.getId());
            sendSuccessMessage("Food menu removed from restaurant successfully", exchange);
        } catch (IOException e) {
            e.printStackTrace();
            internalServerFailureError(e, exchange);
        }

    }
}
