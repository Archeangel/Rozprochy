package MultiThreadChatServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Krzychu on 12.03.2017.
 */
public class UdpHandler implements Runnable {

    DatagramSocket socket;
    private final static int BUFFER = 1024;
    private ArrayList<InetAddress> clientAddresses = new ArrayList<InetAddress>();
    private ArrayList<Integer> clientPorts = new ArrayList<Integer>();
    private HashSet<String> existingClients = new HashSet<String>();

    UdpHandler(DatagramSocket socket) {
        this.socket = socket;
    }

    public void run() {
        byte[] buf = new byte[BUFFER];
        System.out.println("UDP handler running");
        while (true) {
            try {
                Arrays.fill(buf, (byte) 0);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String content = new String(packet.getData(), 0, packet.getLength());
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                String id = clientAddress.toString() + "," + clientPort;
                System.out.println("New Udp message from" + id);
                System.out.println(content);
                if (!existingClients.contains(id)) {
                    existingClients.add(id);
                    clientPorts.add(clientPort);
                    clientAddresses.add(clientAddress);
                }
                byte[] data = (content).getBytes();
                for (int i = 0; i < clientAddresses.size(); i++) {
                    InetAddress cl = clientAddresses.get(i);
                    int cp = clientPorts.get(i);
                    if(!cl.equals(clientAddress) || cp!=clientPort) {
                        packet = new DatagramPacket(data, data.length, cl, cp);
                        socket.send(packet);
                    }
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }
}
