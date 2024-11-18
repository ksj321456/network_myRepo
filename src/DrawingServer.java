import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class DrawingServer extends JFrame {
    private static final int PORT = 12345;
    private final List<ClientHandler> clients = new ArrayList<>();
    private JTextArea t_display;

    public DrawingServer() {
        t_display = new JTextArea();
        add(t_display);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000,500);
        setTitle("서버 GUI");
    }

    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("서버 시작");

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
        private ObjectOutputStream out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                Object message;
                while ((message = in.readObject()) != null) {
                    SketchingData data = (SketchingData) message;
                    // 그리기 모드를 받았다면
                    if (data.getMode() == SketchingData.LINE) {
                        Line line = data.getLine();
                        System.out.println("Line: " + line.getX1() + ", " + line.getY1() + " -> " + line.getX2() + ", " + line.getY2());
                        broadcast(data, this); // 다른 클라이언트들에게 SketchingData 객체 전송
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(SketchingData message, ClientHandler sender) {
            for (ClientHandler client : clients) {
                if (client != sender) {  // 전송자를 제외하고 브로드캐스팅
                    client.sendData(message);
                }
            }
        }

        private void sendData(SketchingData message) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void printDisplay(String msg) {
        t_display.append(msg + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    public static void main(String[] args) {
        DrawingServer drawingServer = new DrawingServer();
        drawingServer.startServer();
    }
}