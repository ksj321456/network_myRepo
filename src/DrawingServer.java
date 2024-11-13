import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class DrawingServer extends JFrame {
    private static final int PORT = 12345;
    private Vector<ClientHandler> clients = new Vector<>();
    private JTextArea t_display;

    public DrawingServer() {
        t_display = new JTextArea();
        add(t_display);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 500);
        setVisible(true);
    }

    private String getLocalAddress() {
        String address = null;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return address;
    }

    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            printDisplay("서버가 시작되었습니다. " + getLocalAddress());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(message, this);  // 다른 클라이언트들에게 좌표 전송
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message, ClientHandler sender) {
            for (ClientHandler client : clients) {
                if (client != sender) {  // 전송자를 제외하고 브로드캐스팅
                    client.sendMessage(message);
                }
            }
        }

        private void sendMessage(String message) {
            out.println(message);
        }
    }
    private void printDisplay(String message) {
        t_display.append(message + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }
    public static void main(String[] args) {
        DrawingServer drawingServer = new DrawingServer();
        drawingServer.startServer();
    }
}
