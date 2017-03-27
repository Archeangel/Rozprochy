package pl.edu.agh.dsrg.sr.chat.service.adapters;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import pl.edu.agh.dsrg.sr.chat.gui.ChatGUI;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.ChatMessage;

public class ChannelReceiverAdapter extends ReceiverAdapter {
    private final JChannel channel;
    private final ChatGUI gui;

    public ChannelReceiverAdapter(JChannel channel, ChatGUI gui) {
        this.channel = channel;
        this.gui = gui;
    }

    @Override
    public void receive(Message msg) {
        try {
            gui.displayMessage(channel.getClusterName(), channel.getName(msg.getSrc()), ChatMessage.parseFrom(msg.getBuffer()).getMessage());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
