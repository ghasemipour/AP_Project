package com.ap.project.httpHandler;

import com.ap.project.dao.UserDao;
import com.ap.project.dto.ProfileDto;
import com.ap.project.entity.user.Admin;
import com.ap.project.entity.user.User;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
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
            }
        }
    }

    private void handleGetListOfUsers(HttpExchange exchange) throws IOException {
        List<ProfileDto> users = UserDao.getListOfUsers(exchange);
        System.out.println(users);
        sendSuccessMessage(new Gson().toJson(users), exchange);
    }
}
