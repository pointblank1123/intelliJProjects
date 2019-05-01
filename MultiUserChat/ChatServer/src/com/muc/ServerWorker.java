package com.muc;

import java.io.*;
import java.net.Socket;

public class ServerWorker extends Thread {
    private final Socket clientSocket;

    public ServerWorker(Socket ClientSocket) {
        this.clientSocket = ClientSocket;
    }
    @Override
    public void run(){
        try {
            handleClientSocket(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void handleClientSocket(Socket clientSocket) throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line=reader.readLine())!= null){
            if("quit".equalsIgnoreCase(line)){
                break;
            }
            String msg = "You typed: " + line + "\n";
            outputStream.write(msg.getBytes());
        }
        //outputStream.write("Hello World\n Alex P.\n".getBytes());
        clientSocket.close();
    }
}
