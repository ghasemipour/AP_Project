package com.ap.project;

import com.ap.project.httpHandler.*;
import com.ap.project.services.AppInitializer;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8000;
        String host = "localhost";

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        AppInitializer.initializeAdmin();

        httpServer.createContext("/auth/register", new RegisterHttpHandler());
        httpServer.createContext("/auth/profile", new ProfileHttpHandler());
        httpServer.createContext("/auth/login", new LoginHttpHandler());
        httpServer.createContext("/auth/logout", new LogoutHttpHandler());
        httpServer.createContext("/restaurants", new RestaurantHttpHandler());
        httpServer.createContext("/items", new FoodHttpHandler());
        httpServer.createContext("/vendors", new RestaurantHttpHandler());
        httpServer.createContext("/orders", new OrderHttpHandler());
        httpServer.createContext("/ratings", new RatingHttpHandler());
        httpServer.createContext("/favorites", new FavoriteHttpHandler());
        httpServer.createContext("/deliveries", new DeliveryHttpHandler());
        httpServer.createContext("/transactions", new TransactionHttpHandler());
        httpServer.createContext("/wallet/top-up", new TransactionHttpHandler());
        httpServer.createContext("/payment/online", new TransactionHttpHandler());
        httpServer.createContext("/admin", new AdminHttpHandler());
        httpServer.createContext("/coupons", new CouponHttpHandler());

        httpServer.setExecutor(null);
        httpServer.start();
        System.out.println("Server started at port " + port);

        }
    }