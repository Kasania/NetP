package com.kasania.server;

import com.kasania.server.ConnectServer;

public class ServerMain {
    public static void main(String[] args) {

        ConnectServer connectionServer = new ConnectServer();

        connectionServer.start();


    }
}
