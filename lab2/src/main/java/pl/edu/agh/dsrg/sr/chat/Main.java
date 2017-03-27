package pl.edu.agh.dsrg.sr.chat;

import pl.edu.agh.dsrg.sr.chat.gui.ChatGUI;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        ChatGUI chat = new ChatGUI();
    }
}
