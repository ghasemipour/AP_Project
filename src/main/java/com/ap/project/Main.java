package com.ap.project;

import com.ap.project.httpHandler.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8000;
        String host = "localhost";

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        httpServer.createContext("/auth/register", new RegisterHttpHandler());
        httpServer.createContext("/auth/profile", new ProfileHttpHandler());
        httpServer.createContext("/auth/login", new LoginHttpHandler());
        httpServer.createContext("/auth/logout", new LogoutHttpHandler());
        httpServer.createContext("/restaurants", new RestaurantHttpHandler());
        httpServer.createContext("/items", new FoodHttpHandler());
        httpServer.createContext("/vendors", new RestaurantHttpHandler());
        httpServer.createContext("/orders", new OrderHttpHandler());
        httpServer.createContext("/favorites", new FavoriteHttpHandler());
        // TODO : handle /coupons

        httpServer.setExecutor(null);
        httpServer.start();
        System.out.println("Server started at port " + port);

        }
    }