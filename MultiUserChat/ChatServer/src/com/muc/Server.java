package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates and opens server for connection
 */

public class Server  extends Thread {
    private final int serverPort;

    private ArrayList<ServerWorker> workerList = new ArrayList<>();

    public Server(int serverPort) {
        this.serverPort=serverPort;
    }

    public List<ServerWorker> getWorkerList(){
        return workerList;
    }

    /**
     * Runs join operation for client to join the server
     */
    @Override
    public void run(){
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true){
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this,clientSocket);
                workerList.add(worker);
                worker.start();
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param serverWorker Client-Server Connection
     * Removes a client from the server
     */
    public void removeWorker(ServerWorker serverWorker) {
        workerList.remove(serverWorker);
    }
}
