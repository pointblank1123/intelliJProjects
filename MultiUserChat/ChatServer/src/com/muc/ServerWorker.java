package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

/**
 * Built operations to be done on the server
 */

public class ServerWorker extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

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

    /**
     *
     * @param clientSocket Users Server Socket
     * @throws IOException
     * @throws InterruptedException
     *
     * Joins client onto server with the port
     */
    private void handleClientSocket(Socket clientSocket) throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); // input sticks here
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
                if ("logout".equalsIgnoreCase(cmd) || "logoff".equalsIgnoreCase(cmd) ||"quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                }else if("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessages(tokensMsg);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                }else if("leave".equalsIgnoreCase(cmd)){
                    handleLeave(tokens);
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

    /**
     *
     * @param tokens Parts of command sent to server
     * Operation to remove user from a created topic
     */
    private void handleLeave(String[] tokens) {
        if(tokens.length > 1){
            String topic = tokens[1];
            topicSet.remove(topic);

        }
    }

    public boolean isMemberOfTopic(String topic){
        return topicSet.contains(topic);
    }

    /**
     *
     * @param tokens Parts of command sent to server
     * Handles Users joining a topic
     */
    private void handleJoin(String[] tokens) {
        if(tokens.length > 1){
            String topic = tokens[1];
            topicSet.add(topic);

        }
    }
    // format: "msg" "login" body...
    // format: "msg" "#topic" body...

    /**
     *
     * @param tokens Parts of command sent to server
     * @throws IOException
     * Handles messages between users and within topics
     */
    private void handleMessages(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';
        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList){
            if(isTopic){
                if(worker.isMemberOfTopic(sendTo)){
                    String outMsg = "msg " + sendTo +":"+ login + ": " + body + "\n";
                    worker.send(outMsg);
                }
            }
            else {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "msg " + login + ": " + body + "\n";
                    worker.send(outMsg);
                }
            }
        }
    }

    /**
     *
     * @throws IOException
     * Handles User logoff
     */
    private void handleLogoff() throws IOException {
        server.removeWorker(this);
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

    /**
     *
     * @param outputStream
     * @param tokens
     * @throws IOException
     * Handles User login
     */
    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if(tokens.length == 3){
            String login =tokens[1];
            String password = tokens[2];

            if((login.equals("guest") && password.equals("guest")) || (login.equals("alex") && password.equals("alex"))){
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
                System.err.println("Login failed for " + login);
            }
        }
    }

    private void send(String onlineMsg) throws IOException {
        if(login != null) {
            outputStream.write(onlineMsg.getBytes());
        }
    }
}
