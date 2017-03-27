package pl.edu.agh.dsrg.sr.chat.gui;

import pl.edu.agh.dsrg.sr.chat.service.ChatService;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

public class ChatGUI extends JFrame {
    private JTabbedPane tabsPane;
    private Map<String, ChannelPanel> tabs;
    private JoinPanel joinPanel;
    private ChatService chatService;

    public ChatGUI() throws Exception {
        super("Chat");

        chatService = new ChatService();
        chatService.setChatGUI(this);
        chatService.setNick(getNick());

        tabs = new HashMap<>();

        createGUI();

        chatService.joinManagementChnanel();
    }

    private String getNick() throws Exception {
        String nick = "";

        while (nick.length() <= 0) {
            nick = JOptionPane.showInputDialog(
                    this, "Enter your  nick", "Nick"
            );

            if (nick == null) {
                System.exit(1);
            }
        }

        return nick;
    }

    private void createGUI() {
        tabsPane = new JTabbedPane();
        add(tabsPane);

        joinPanel = new JoinPanel(chatService);
        tabsPane.add("Join", joinPanel);

        pack();
        setSize(500, 500);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                chatService.leaveManagementChannel();
                System.exit(0);
            }
        });
        setVisible(true);
    }

    public void addChannel(String channelName) {
        ChannelPanel panel = new ChannelPanel(chatService, channelName);
        tabs.put(channelName, panel);
        joinPanel.addChannel(channelName);
    }

    public void joinChannel(String channelName) {
        ChannelPanel panel = tabs.get(channelName);
        tabsPane.add(panel, channelName);
        tabsPane.setSelectedComponent(panel);
    }

    public void leaveChannel(String channelName) {
        ChannelPanel panel = tabs.get(channelName);
        panel.clear();
        tabsPane.setSelectedIndex(0);
        tabsPane.remove(panel);
    }

    public void displayMessage(String channelName, String nick, String text) {
        ChannelPanel panel = tabs.get(channelName);
        if (panel.isDisplayable()) {
            panel.append(new String("<" + nick + ">: " + text + "\n"));
        }
    }

    public void addUser(String channelName, String nick) {
        ChannelPanel panel = tabs.get(channelName);
        panel.addUser(nick);

        if (panel.isDisplayable()) {
            panel.append("New user - " + nick + " joined");
        }
    }

    public void removeUser(String channelName, String nick) {
        ChannelPanel panel = tabs.get(channelName);
        panel.removeUser(nick);

        if (panel.isDisplayable()) {
            panel.append("User " + nick + " left");
        }
    }

    public void removeChannel(String channelName) {
        tabs.remove(channelName);
        joinPanel.removeChannel(channelName);
    }
}
