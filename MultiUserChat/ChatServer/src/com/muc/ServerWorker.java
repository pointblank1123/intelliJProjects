package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;


public class ServerWorker extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket ClientSocket) {
        this.clientSocket = ClientSocket;
        this.server = server;
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

    private void handleClientSocket(Socket clientSocket) throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line=reader.readLine())!= null){
            String[] tokens = StringUtils.split(line);
            /*  String Utils, Apache Class
                Download from commons.apache.org/proper/commons-lang/download_lang.cgi
                Extract: intelliJ File/Project Structure click + and find commons-lang#.#.#.jar
                ----------------Method 2---------------
                Type String[] tokens = Line.split("");
             */
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(cmd)) {
                    break;
                } else if("login".equalsIgnoreCase(cmd)){
                    handleLogin(outputStream, tokens);
                } else {
                    String msq = "Unknown Command " + cmd + "\n";
                    outputStream.write(msq.getBytes());
                }
                /*
                String msg = "You Typed: " + line + "\n";
                outputStream.write(msg.getBytes());
                 */
            }
        }
        //outputStream.write("Hello World\n Alex P.\n".getBytes());
        clientSocket.close();
    }

    private String getLogin(){
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if(tokens.length == 3){
            String login =tokens[1];
            String password = tokens[2];

            if((login.equals("guest") && password.equals("guest")) || (login.equals("jim") && password.equals("jim"))){
                String msg = "Ok Login\n ";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User Logged In Successfully :" + login);

                String onlineMsg = "Online " + login +"\n";
                List<ServerWorker> workerList = server.getWorkerList();
                for(ServerWorker worker: workerList) {
                    worker.send(onlineMsg);
                }
            } else{
                String msg = "Error Login\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String onlineMsg) throws IOException {
        outputStream.write(onlineMsg.getBytes());

    }
}
