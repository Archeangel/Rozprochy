package pl.edu.agh.dsrg.sr.chat.service.adapters;

import org.jgroups.*;
import pl.edu.agh.dsrg.sr.chat.gui.ChatGUI;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction.ActionType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatAction;
import static pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatState;

public class ManagementReceiverAdapter extends ReceiverAdapter {
    private final Map<String, List<String>> channelUsers;
    private final ChatGUI gui;
    private final JChannel managementChannel;

    public ManagementReceiverAdapter(Map<String, List<String>> channelUsers, ChatGUI gui,
                                     JChannel managementChannel) {
        this.channelUsers = channelUsers;
        this.gui = gui;
        this.managementChannel = managementChannel;
    }

    @Override
    public void receive(Message msg) {
        synchronized (channelUsers) {
            ChatAction chatAction;

            try {
                chatAction = ChatAction.parseFrom(msg.getBuffer());

                ActionType actionType = chatAction.getAction();
                String channelName = chatAction.getChannel();
                String nick = chatAction.getNickname();

                switch (actionType) {
                    case JOIN:
                        if (channelUsers.containsKey(channelName) == false) {
                            channelUsers.put(channelName, new ArrayList<String>());
                            gui.addChannel(channelName);
                        }
                        channelUsers.get(channelName).add(nick);
                        gui.addUser(channelName, nick);
                        break;
                    case LEAVE:
                        channelUsers.get(channelName).remove(nick);
                        gui.removeUser(channelName, nick);
                        if (channelUsers.get(channelName).isEmpty()) {
                            channelUsers.remove(channelName);
                            gui.removeChannel(channelName);
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (channelUsers) {
            ChatState.Builder builder = ChatState.newBuilder();
            for (Map.Entry<String, List<String>> entry : channelUsers.entrySet()) {
                String channelName = entry.getKey();
                List<String> users = entry.getValue();
                for (String user : users) {
                    builder.addStateBuilder()
                            .setAction(ActionType.JOIN)
                            .setChannel(channelName)
                            .setNickname(user);
                }
            }
            ChatState state = builder.build();
            state.writeTo(output);
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        synchronized (channelUsers) {
            ChatState state = ChatState.parseFrom(input);
            channelUsers.clear();
            for (ChatAction chatAction : state.getStateList()) {
                String channelName = chatAction.getChannel();
                String nick = chatAction.getNickname();
                if (channelUsers.containsKey(channelName) == false) {
                    channelUsers.put(channelName, new LinkedList<String>());
                    gui.addChannel(channelName);
                }
                channelUsers.get(channelName).add(nick);
                gui.addUser(channelName, nick);
            }
        }
    }

    @Override
    public void viewAccepted(View view) {
        synchronized (channelUsers) {
            List<String> currentUsers = new ArrayList<String>();
            for (Address memberAddress : view.getMembers()) {
                currentUsers.add(managementChannel.getName(memberAddress));
            }
            for (Map.Entry<String, List<String>> entry : channelUsers.entrySet()) {
                String channelName = entry.getKey();
                List<String> lostUsers = new ArrayList(entry.getValue());
                lostUsers.removeAll(currentUsers);

                for (String lostUser : lostUsers) {
                    gui.removeUser(channelName, lostUser);
                }
                entry.getValue().retainAll(currentUsers);
            }
        }
    }
}
