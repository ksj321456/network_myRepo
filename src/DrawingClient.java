import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.net.Socket;

public class DrawingClient extends JFrame {
    //    private static final String SERVER_ADDRESS = "localhost";
//    private static final int SERVER_PORT = 12345;
    private String serverAddress = "localhost";
    private int serverPort = 12345;
    private String userId; // 사용자 ID

    private Socket socket;
    private ObjectOutputStream out;
    private DrawPanel drawPanel;    // 그림판 Panel
    private ChatingListPanel chatingListPanel; // 채팅창 Panel
    private LeftUserPanel leftUserPanel;
    private RightUserPanel rightUserPanel;
    private DrawingSetting drawingSetting;

    private boolean isDrawing = false;      // 그림 그리고 있는지 확인
    private Point lastPoint = null;  // 마지막 좌표


    public DrawingClient(String userId, String serverAddress, int serverPort) {
        this.userId = userId;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        buildGUI(); // GUI 구성
        connectToServer();
    }

    private void buildGUI() {
        setResizable(false);
        setTitle("Hansung Sketch");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        drawPanel = new DrawPanel();
        leftUserPanel = new LeftUserPanel();
        rightUserPanel = new RightUserPanel();
        chatingListPanel = new ChatingListPanel();
        drawingSetting = new DrawingSetting(this);
        InputPanel inputPanel = new InputPanel(this);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(drawPanel, BorderLayout.CENTER);
        centerPanel.add(leftUserPanel, BorderLayout.WEST);
        centerPanel.add(rightUserPanel, BorderLayout.EAST);
        centerPanel.add(drawingSetting, BorderLayout.NORTH);

        add(centerPanel);
        add(inputPanel, BorderLayout.SOUTH);
        add(chatingListPanel, BorderLayout.EAST);
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
                Color selectedColor = drawingSetting.getSelectedColor();
                float selectedWidth = drawingSetting.getSelectedLineWidth();

                // Line 객체 생성 및 색깔, 굵기 설정
                Line line = new Line(lastPoint.x, lastPoint.y, e.getX(), e.getY(), selectedColor, selectedWidth);

                // SketchingData 객체 생성
                SketchingData sketchingData = new SketchingData(SketchingData.MODE_LINE, line);

                // 서버에 Line 객체 전송
                try {
                    out.writeObject(sketchingData);
                    out.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                // 마지막 좌표를 현재 좌표로 업데이트
                lastPoint = new Point(e.getX(), e.getY());
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


    //UserPanel에 사용자 추가하는 메서드
    private void addUser(String userId) {
        if (!leftUserPanel.addUser(userId, 0)) {
            rightUserPanel.addUser(userId, 0);
        }
    }

    //UserPanel에 사용자 제거하는 메서드
    private void removeUser(String userId) {
        if (!leftUserPanel.removeUser(userId)) {
            rightUserPanel.removeUser(userId);
        }
    }

    private void connectToServer() {
        // Swing은 단일 스레드 환경임. EDT라는 스레드에서 혼자 UI 업데이트를 처리.
        // 이때, 서버에 접속하는 네트워크 작업을 EDT에서 수행하게되면 UI 업데이트가 블록되어짐. 즉 컴포넌트들이 렌더링되지 않음.
        // so, 별도의 스레드에서 네트워크 작업을 수행해야 함.
        new Thread(() -> {
            try {
                socket = new Socket(serverAddress, serverPort);
                out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                //out.flush();

                //순서: 그리기를 실행하는 스레드 실행 후 -> 데이터 수신 스레드 실행. 역순으로하게되면 채팅 전송하기전까지 그림을 그릴 수 없음.
                DrawingThread drawingThread = new DrawingThread();
                drawingThread.start();

                Thread sendCoordsThread = new ReceiveThread(socket);
                sendCoordsThread.start(); //ObjectOutputStream을 ObjectInputStream보다 먼저 생성해야 함. 미준수시 데드락 발생 가능성 있음.

                // 서버에 접속한 사용자를 UserPanel에 추가
                addUser(userId);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 메세지를 수신하는 스레드
    private class ReceiveThread extends Thread {
        private ObjectInputStream in;

        public ReceiveThread(Socket socket) throws IOException {
            in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        }

        @Override
        public void run() {
            SketchingData data;
            try {
                while ((data = (SketchingData) in.readObject()) != null) {
                    // 그리기 모드를 받았을 때
                    if (data.getMode() == SketchingData.MODE_LINE) {
                        Line line = data.getLine();
                        drawPanel.addLine(line.getX1(), line.getY1(), line.getX2(), line.getY2(), line.getColor(), line.getLineWidth());
                    }
                    // 채팅 모드를 받았을 때
                    else if (data.getMode() == SketchingData.MODE_CHAT) {
                        chatingListPanel.addMessage(data.getMessage());
                    } else if (data.getMode() == SketchingData.MODE_LOGOUT) {
                        removeUser(data.getUserID());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void disconnect() {
        send(new SketchingData(SketchingData.MODE_LOGOUT, userId));
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("클라이언트 닫기 오류> " + e.getMessage());
            System.exit(-1);
        }
        //다시 로그인 화면으로 돌아가기. ClientMain으로 전환.//ClientMain.setVisible(true);
    }

    //    public void sendMessage(String message) {
//        String fullMessage = userId + ": " + message;
//        SketchingData chatData = new SketchingData(SketchingData.MODE_CHAT, userId, fullMessage);
//        chatingListPanel.addMessage("나: " + message);
//
//        try {
//            out.writeObject(chatData);
//            out.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(0);
//        }
//    }
    private void send(SketchingData msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.err.println("클라이언트 일반 전송 오류> " + e.getMessage());
            System.exit(0);
        }
    }


//
//    public static void main(String[] args) {
//        String userId = JOptionPane.showInputDialog("사용자 ID를 입력하세요:"); // 다이얼로그로 사용자 ID 입력
//        DrawingClient drawingClient = new DrawingClient(userId);
//        //drawingClient.connectToServer();
//    }
}