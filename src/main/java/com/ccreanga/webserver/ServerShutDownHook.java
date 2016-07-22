package com.ccreanga.webserver;

/**
 * Close the log on server shutdown
 */
public class ServerShutDownHook extends Thread{

    public void run() {
        System.out.println("Server is  shutting down.");
    }
}
