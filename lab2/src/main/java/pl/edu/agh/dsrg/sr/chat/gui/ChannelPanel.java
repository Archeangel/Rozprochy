package pl.edu.agh.dsrg.sr.chat.gui;

import pl.edu.agh.dsrg.sr.chat.service.ChatService;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

public class ChannelPanel extends JPanel {
    private final String channelName;
    private final ChatService chatService;
    private JTextArea chat = new JTextArea();
    private DefaultListModel<String> usersModel = new DefaultListModel<>();

    public ChannelPanel(ChatService chatService, String channelName) {
        super(new GridLayout(2, 1));

        this.chatService = chatService;
        this.channelName = channelName;

        createGUI();
    }

    private void createGUI() {
        JPanel overwiewPanel = new JPanel(new GridLayout(1, 2));
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));

        JScrollPane scrollChat = new JScrollPane();
        JScrollPane scrollUsers = new JScrollPane();

        JList<String> users = new JList<>(usersModel);

        chat.setWrapStyleWord(true);
        chat.setLineWrap(true);
        chat.setEditable(false);
        chat.setFont(new Font(Font.SERIF, Font.ROMAN_BASELINE, 12));

        DefaultCaret caret = (DefaultCaret) chat.getCaret();
        caret.setUpdatePolicy(Rectangle2D.OUT_BOTTOM);

        scrollChat.setViewportView(chat);
        scrollUsers.setViewportView(users);
        final JTextField sendText = new JTextField();
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = sendText.getText();
                try {
                    chatService.sendMessage(channelName, message);
                    sendText.setText("");
                } catch (Exception e1) {
                    System.out.println(e);
                }
            }
        };
        JButton leaveButton = new JButton("Leave channel");
        leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    chatService.leaveChannel(channelName);
                } catch (Exception e1) {
                    System.out.println(e);
                }
            }
        });
        sendText.addActionListener(actionListener);

        add(scrollChat);
        overwiewPanel.add(users);
        overwiewPanel.add(sendText);
        add(overwiewPanel);
        buttonsPanel.add(leaveButton);
        add(buttonsPanel);
        add(sendText);


    }

    public void clear() {
        chat.setText("");
    }

    public void append(String line) {

        chat.append(line);
    }

    public void addUser(String nick) {
        usersModel.addElement(nick);
    }

    public void removeUser(String nick) {

        usersModel.removeElement(nick);
    }
}
