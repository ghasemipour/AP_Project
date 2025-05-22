package com.ap.project.httpHandler;


import com.ap.project.dao.RegisterDao;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RegisterHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        RegisterDao req = new Gson().fromJson(reader, RegisterDao.class);
        //TODO: Handling responses

    }
}
