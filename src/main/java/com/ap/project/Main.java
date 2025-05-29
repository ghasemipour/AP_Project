package com.ap.project;

import com.ap.project.httpHandler.LoginHttpHandler;
import com.ap.project.httpHandler.ProfileHttpHandler;
import com.ap.project.httpHandler.RegisterHttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8000;
        String host = "localhost";

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        httpServer.createContext("/auth/register", new RegisterHttpHandler());
        /*TODO : Write Login endpoints */
        httpServer.createContext("/auth/profile", new ProfileHttpHandler());
        /* TODO: Write Other endpoints */
        httpServer.createContext("/auth/login", new LoginHttpHandler());
        /* TODO: Write Other endpoints */

        httpServer.setExecutor(null);
        httpServer.start();
        System.out.println("Server started at port " + port);

        }
    }