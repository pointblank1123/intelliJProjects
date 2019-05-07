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
                if ("logoff".equalsIgnoreCase(cmd) ||"quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
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

    private void handleLogoff() throws IOException {
        List<ServerWorker> workerList = server.getWorkerList();
        // send other online users current user status
        String onlineMsg = "Offline " + login +"\n";
        for(ServerWorker worker: workerList){
            if(!login.equals(worker.getLogin())) {// so that this doesn't display your own login
                worker.send(onlineMsg);
            }
        }
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
                // basic login takes first 3 input words and first is command second and third are user and password
                String msg = "Ok Login\n ";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User Logged In Successfully :" + login);

                //gives notification of all others logged in
                List<ServerWorker> workerList = server.getWorkerList();
                for(ServerWorker worker: workerList) { //array for loop
                    if(worker.getLogin() != null) { // eliminates null for user login
                        if(!login.equals(worker.getLogin())) { // so that this doesn't display your own login
                            String msg2 = "Online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }
                // send other online users current user status
                String onlineMsg = "Online " + login +"\n";
                for(ServerWorker worker: workerList){
                    if(!login.equals(worker.getLogin())) {// so that this doesn't display your own login
                        worker.send(onlineMsg);
                    }
                }
            } else{
                String msg = "Error Login\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String onlineMsg) throws IOException {
        if(login != null) {
            outputStream.write(onlineMsg.getBytes());
        }
    }
}
