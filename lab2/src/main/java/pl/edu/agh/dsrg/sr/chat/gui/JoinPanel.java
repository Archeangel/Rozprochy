package pl.edu.agh.dsrg.sr.chat.gui;

import pl.edu.agh.dsrg.sr.chat.service.ChatService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class JoinPanel extends JPanel {
    private final ChatService chatService;
    private JComboBox<String> channelInput;
    private DefaultComboBoxModel<String> channelsModel = new DefaultComboBoxModel<>();

    public JoinPanel(ChatService service) {
        super(new GridLayout(1, 1));
        chatService = service;

        createGUI();
    }

    private void createGUI() {
        channelInput = new JComboBox<>(channelsModel);
        channelInput.setEditable(true);
        channelInput.setSelectedItem("");
        add(channelInput);
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String actionCommand = e.getActionCommand();
               if(!actionCommand.equals("comboBoxEdited"))
                   return;

                final String channelName = (String) channelInput.getSelectedItem();

                if (channelName == null) {
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            chatService.joinChannel(channelName);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        channelInput.setSelectedItem("");
                    }
                }).start();
            }
        };

        channelInput.addActionListener(actionListener);
    }

    public void addChannel(String channelName) {

        channelsModel.addElement(channelName);
    }

    public void removeChannel(String channelName) {

        channelsModel.removeElement(channelName);
    }
}
