package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Creates User List Pane Gui JFrame
 */
public class UserListPane extends JPanel implements UserStatusListener {

    private final ChatClient client;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;

    /**
     *
     * @param client User Port
     */
    public UserListPane(ChatClient client) {
        this.client = client;
        this.client.addUserStatusListener(this);

        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() > 1){
                   String login = userListUI.getSelectedValue();
                   MessagePane messagePane = new MessagePane(client, login);

                   JFrame f = new JFrame("Message: " + login);
                   f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                   f.setSize(500,500);
                   f.getContentPane().add(messagePane, BorderLayout.CENTER);
                   f.setVisible(true);
                }
            }
        });

    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 8555);

        UserListPane userListPane = new UserListPane(client);
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);


        frame.getContentPane().add(userListPane, BorderLayout.CENTER);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    client.logoff();
                    System.out.println("Logged off");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        if(client.connect()){
            try {
                client.login("guest", "guest");
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public void online(String login) {

        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {

        userListModel.removeElement(login);
    }
}
