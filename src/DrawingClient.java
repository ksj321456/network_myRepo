import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class DrawingClient extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private DrawPanel drawPanel;    // 그림판 Panel
    private ChatingListPanel chatingListPanel; // 채팅창 Panel
    private String userId; // 사용자 ID
    private LeftUserPanel leftUserPanel;
    private RightUserPanel rightUserPanel;
    private DrawingSetting drawingSetting;

    private boolean isDrawing = false;      // 그림 그리고 있는지 확인
    private Point lastPoint = null;  // 마지막 좌표

    public DrawingClient(String userId) {
        this.userId = userId;
        setResizable(false);
        setTitle("Hansung Sketching");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        drawPanel = new DrawPanel();
        leftUserPanel = new LeftUserPanel();
        rightUserPanel = new RightUserPanel();
        chatingListPanel = new ChatingListPanel();
        drawingSetting = new DrawingSetting();
        InputPanel inputPanel = new InputPanel(this);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(drawPanel, BorderLayout.CENTER);
        centerPanel.add(leftUserPanel, BorderLayout.WEST);
        centerPanel.add(rightUserPanel, BorderLayout.EAST);
        centerPanel.add(drawingSetting, BorderLayout.NORTH);

        add(centerPanel);

        DrawingThread drawingThread = new DrawingThread();
        drawingThread.start();

        add(inputPanel, BorderLayout.SOUTH);
        add(chatingListPanel, BorderLayout.EAST);

        connectToServer();
        setVisible(true);
    }

    // 그리기를 실행하는 Thread
    private class DrawingThread extends Thread implements MouseMotionListener, MouseListener {
        private MouseEvent currentEvent;

        @Override
        public void run() {
            drawPanel.addMouseMotionListener(this);
            drawPanel.addMouseListener(this);
        }

        // MouseMotionListener 구현
        @Override
        public void mouseDragged(MouseEvent e) {
            if (isDrawing && lastPoint != null) { // 그리기 상태일 때만 좌표를 전송
                String message = lastPoint.x + "," + lastPoint.y + "," + e.getX() + "," + e.getY();
                out.println(message); // 서버로 좌표 전송

                // 마우스 이벤트를 발생시킨 클라이언트는 즉시 좌표를 그림에 추가
                drawPanel.addLine(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                lastPoint = new Point(e.getX(), e.getY()); // 마지막 좌표 갱신
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        // MouseListener 구현
        @Override
        public void mousePressed(MouseEvent e) {
            isDrawing = true; // 그리기 시작
            lastPoint = new Point(e.getX(), e.getY()); // 현재 좌표 저장
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            isDrawing = false; // 그리기 상태 종료
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }


    public void sendMessage(String message) {
        String fullMessage = userId + ": " + message;
        chatingListPanel.addMessage("나: " + message);
        out.println(fullMessage);  // 서버로 메시지 전송
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);

            Thread sendCoordsThread = new ReceiveThread(socket);
            sendCoordsThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 메세지를 수신하는 스레드
    private class ReceiveThread extends Thread {
        private final BufferedReader in;

        public ReceiveThread(Socket socket) throws IOException {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    String[] coords = message.split(",");
                    if (coords.length == 4) {
                        int x1 = Integer.parseInt(coords[0]);
                        int y1 = Integer.parseInt(coords[1]);
                        int x2 = Integer.parseInt(coords[2]);
                        int y2 = Integer.parseInt(coords[3]);
                        drawPanel.addLine(x1, y1, x2, y2);  // 받은 좌표로 선 그리기
                    } else {
                        String[] parts = message.split(": ");
                        if (parts.length == 2) {
                            String senderId = parts[0];
                            String chatMessage = parts[1];
                            if (!senderId.equals(userId)) {
                                chatingListPanel.addMessage(senderId + ": " + chatMessage);  // 상대방 메시지 추가
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String userId = JOptionPane.showInputDialog("사용자 ID를 입력하세요:"); // 다이얼로그로 사용자 ID 입력
        DrawingClient drawingClient = new DrawingClient(userId);
        //drawingClient.connectToServer();
    }
}