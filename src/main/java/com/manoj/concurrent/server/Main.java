package com.manoj.concurrent.server;


import com.manoj.concurrent.server.server.TCPSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        log.info("Starting TCP server....");
        TCPSocketServer server = new TCPSocketServer();

        try {
            server.startServer();
        } catch (IOException e){
            log.error("Starting NIO server failed.",e);
        }
    }
}