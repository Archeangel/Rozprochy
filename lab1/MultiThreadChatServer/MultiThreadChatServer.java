package MultiThreadChatServer;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.ServerSocket;


//  A MultiThread chat server

public class MultiThreadChatServer {

    // The udp server socket.
    private static DatagramSocket updSocket = null;
    // The tcp server socket.
    private static ServerSocket serverTcpSocket = null;
    // The tcp client socket.
    private static Socket clientTcpSocket = null;

    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int MAX_CLIENT_COUNT = 4;
    private static final clientThread[] threads = new clientThread[MAX_CLIENT_COUNT];

    public static void main(String args[]) {

        // The  port number.
        int portNumber = 8888;


        //Open a server socket on the portNumber (2500).

        try {
            updSocket = new DatagramSocket(portNumber + 1);
            serverTcpSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }
        while (true) {
            try {
                clientTcpSocket = serverTcpSocket.accept();
                int i = 0;
                for (i = 0; i < MAX_CLIENT_COUNT; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new clientThread(clientTcpSocket, threads, i, updSocket)).start();
                        break;
                    }
                }
                if (i == MAX_CLIENT_COUNT) {
                    PrintStream os = new PrintStream(clientTcpSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientTcpSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}


class clientThread extends Thread {

    private DataInputStream is = null;
    private PrintStream os = null;
    private Socket clientTcpSocket = null;
    private DatagramSocket udpSocket = null;

    private final clientThread[] threads;
    private int maxClientsCount;
    private int id;

    public clientThread(Socket clientTcpSocket, clientThread[] threads, int id, DatagramSocket udpSocket) {
        this.clientTcpSocket = clientTcpSocket;
        this.threads = threads;
        this.maxClientsCount = threads.length;
        this.id = id;
        this.udpSocket = udpSocket;
    }

    public void run() {

        try {
      /*
       * Create input and output streams for this client.
       */
            is = new DataInputStream(clientTcpSocket.getInputStream());
            os = new PrintStream(clientTcpSocket.getOutputStream());

            os.println("Enter your nick.");
            String nick = is.readLine();
            new Thread(new UdpHandler(udpSocket)).start();
            os.println("You connected to the chat room");
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && threads[i] != this) {
                    threads[i].os.println(nick + " joined the chat room!");
                }
            }
            while (true) {
                String line = is.readLine();
                if (line.equals("exit")) {
                    break;
                } else {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i] != this) {
                            threads[i].os.println("<" + nick + ">:" + line);
                        }
                    }
                }
            }
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && threads[i] != this) {
                    threads[i].os.println(nick + " is leaving the chat room!");
                }
            }
            os.println("You disconnected from the chat room");
            threads[this.id] = null;

            is.close();
            os.close();
            clientTcpSocket.close();
        } catch (IOException e) {
        }
    }
}
