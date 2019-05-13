package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final int serverPort;
    private final String serverName;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: "+ login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });
        if(!client.connect()){
            System.err.println("Connect Failed");
        }else{
            System.err.println("Connect successful");
            if(client.login("guest", "guest")) {
                System.out.println("Login successful");
            }else{
                System.err.println("Login Failed");
            }

        }
    }

    private boolean login(String login, String password) throws IOException {
        String cmd = "login " + login +" "+ password;
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Lines:" + response);

        if("ok login".equalsIgnoreCase(response)){
            startMessageReader();
            return true;
        }else{
            return false;
        }
    }

    private void startMessageReader() {
        Thread t = new Thread(){
            @Override
            public void run(){
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if(tokens != null && tokens.length>0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    }else if("offline".equalsIgnoreCase(cmd)){
                        handleOffline(tokens);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners){
            listener.online(login);
        }
    }

    private boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is: "+ socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void addUserStatusListener(UserStatusListener listener){
        userStatusListeners.add(listener);
    }
    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListeners.remove(listener);
    }
}
