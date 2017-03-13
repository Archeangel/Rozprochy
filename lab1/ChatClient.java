import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class ChatClient implements Runnable {
    final static String INET_ADDR = "224.0.0.3";
    private static String nick;
    final static int PORT = 8888;
    private static final String host = "localhost";
    private static Socket clientTcpSocket = null;
    private static InetAddress multiAddress = null;
    private static MulticastSocket multicastUdpSocket = null;
    private static MulticastSocket unicastUdpSocket = null;
    private static PrintStream tcpOutput = null;
    private static DataInputStream tcpInput = null;
    private static BufferedReader input = null;
    private static boolean closed = false;
    private static final String ASCII_ART = "\n" +
            " ░░░░░░░░▄▄▀▀█▀▀▀▀▀▄▄\n" +
            " ░░░░░░▄▀▓░░▒░░▒▒▒▒▒▒█▄░░░░░░\n" +
            " ░░░░▄█▓▓▓░░░░▒▒▒▒▒▒▒▒█▀▄░░░░\n" +
            " ░░▄▀█▌▓▓▓░░░░▒▒▒▒▒▒▒▒▐▌▓▀▄░░\n" +
            " ░█▓▓█▌▓▄▄▓░░░▒▒▒▒▄▄▒▒▒█▓▓▀▄░\n" +
            " ▄▀▓▓█▌▓▀█▓░░░▒▒▒▒█▓▀▒▄▌▓▓▓▓█\n" +
            " █▓▓▓▄▀▓▓▓▓░░░▒▒▒▒▒▒▒▒░░▌▓▓▓█\n" +
            " ▀▄▓▓█░▀▓▓░░░░░░░▒▒▒▒▒░▄▌▓▓█░\n" +
            " ░█▓▓▓█░▓░░░░░░░░░▒▒▒░░█▓▓▓█░\n" +
            " ░▀▄▓▓█░▐░░▄▄███▄░░░▐░░░▀▄▀░░\n" +
            " ░░▀▄▄▀░▐░░█████▀░░▄▀░░░░░░░░\n" +
            " ░░░░░░░░▀░░▀██▀░▄▀";

    public static void main(String[] args) {


        // Open a socket on a given host and port. Open input and output streams.

        try {
            InetAddress address = InetAddress.getByName(INET_ADDR);
            multicastUdpSocket = new MulticastSocket(PORT);
            unicastUdpSocket=new MulticastSocket();
            multiAddress = InetAddress.getByName(INET_ADDR);
            multicastUdpSocket.joinGroup(multiAddress);
            clientTcpSocket = new Socket(host, PORT);
            input = new BufferedReader(new InputStreamReader(System.in));
            tcpOutput = new PrintStream(clientTcpSocket.getOutputStream());
            tcpInput = new DataInputStream(clientTcpSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host "
                    + host);
        }
        if (clientTcpSocket != null && tcpOutput != null && tcpInput != null) {
            try {
                sendUdpMessage("New person connected by UDP!".getBytes(), true);
        /* Create a thread to read from the server. */
                new Thread(new udpReceiver(multicastUdpSocket)).start();
                new Thread(new udpReceiver(unicastUdpSocket)).start();
                new Thread(new ChatClient()).start();
                while (!closed) {
                    String inputLine = input.readLine();
                    if (nick == null)
                        nick = new String(inputLine);
                    if (inputLine.contains("-M")) {
                        sendUdpMessage(("<" + nick + ">: " + ASCII_ART).getBytes(), true);

                    } else if (inputLine.contains("-N")) {
                        sendUdpMessage(("<" + nick + ">: " + ASCII_ART).getBytes(), false);
                    } else {
                        tcpOutput.println(inputLine);
                    }
                }

                tcpOutput.close();
                unicastUdpSocket.close();
                multicastUdpSocket.leaveGroup(multiAddress);
                multicastUdpSocket.close();
                tcpInput.close();
                clientTcpSocket.close();
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }

    private static void sendUdpMessage(byte[] sendBuffer, boolean isUnicast) throws IOException {
        if (isUnicast) {
            InetAddress address = InetAddress.getByName(host);
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, PORT + 1);
            unicastUdpSocket.send(sendPacket);
        } else {
            multicastUdpSocket.send(new DatagramPacket(sendBuffer, sendBuffer.length, multiAddress, PORT));

        }
    }

    public void run() {
        String responseLine;
        try {
            while ((responseLine = tcpInput.readLine()) != null) {
                System.out.println(responseLine);
                if (responseLine.equals("You disconnected from the chat room"))
                    break;
            }
            closed = true;
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
}

class udpReceiver implements Runnable {
    DatagramSocket udpSocket = null;

    udpReceiver(DatagramSocket udpSocket1) {
        this.udpSocket = udpSocket1;

    }

    public void run() {
        System.out.println("UDP receiver running");
        byte[] responseBuffer = new byte[1024];
        while (true) {
            Arrays.fill(responseBuffer, (byte) 0);
            DatagramPacket receiveResponse = new DatagramPacket(responseBuffer, responseBuffer.length);
            try {
                udpSocket.receive(receiveResponse);
                System.out.println(new String(receiveResponse.getData()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}