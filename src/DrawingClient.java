import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.net.Socket;

public class DrawingClient extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private ObjectOutputStream out;
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
                Line line = new Line(lastPoint.x, lastPoint.y, e.getX(), e.getY());
                SketchingData sketchingData = new SketchingData(SketchingData.LINE, line);

                // 서버에 Line 객체 전송
                try {
                    out.writeObject(sketchingData);
                    out.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
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
            out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            Thread sendCoordsThread = new ReceiveThread(socket);
            sendCoordsThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 메세지를 수신하는 스레드
    private class ReceiveThread extends Thread {
        private ObjectInputStream in;

        public ReceiveThread(Socket socket) throws IOException {
            in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        }

        @Override
        public void run() {
            Object message;
            try {
                while ((message = in.readObject()) != null) {
                    SketchingData data = (SketchingData) message;
                    // 그리기 모드를 받았을 때
                    if (data.getMode() == SketchingData.LINE) {
                        Line line = data.getLine();
                        drawPanel.addLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        String userId = JOptionPane.showInputDialog("사용자 ID를 입력하세요:"); // 다이얼로그로 사용자 ID 입력
        DrawingClient drawingClient = new DrawingClient(userId);
        //drawingClient.connectToServer();
    }
}