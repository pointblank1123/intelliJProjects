package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 *
 *  Gui login window to open user list pane
 *
 */
public class LoginWindow extends JFrame {
    private final ChatClient client;
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");

    /**
     * Builds JFrame window for login
     */
    public LoginWindow(){
        super("login");

        this.client = new ChatClient("localhost", 8555);
        client.connect();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(loginButton);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });


                getContentPane().add(p, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    /**
     * Tests login and send to user list pane
     */
    private void doLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        try{
            if(client.login(login,password)){
                //Bring up list window
                UserListPane userListPane = new UserListPane(client);
                JFrame frame = new JFrame("User List");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 600);


                frame.getContentPane().add(userListPane, BorderLayout.CENTER);
                frame.setVisible(true);
                setVisible(false);
            } else {
                // show error message
                JOptionPane.showMessageDialog(this, "Invalid Login/Password.");
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        LoginWindow loginWin = new LoginWindow();
        loginWin.setVisible(true);


    }
}
