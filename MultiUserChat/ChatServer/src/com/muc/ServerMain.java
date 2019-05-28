package com.muc;

/**
 * Created 04-15-19
 *
 * Main class to open the chat server side
*/
public class ServerMain {
    public static void main(String[] args){
        int port = 8555;
        Server server = new Server(port);

        server.start();
    }


}