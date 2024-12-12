import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Vector;

public class DrawingClient extends JFrame {
    //    private static final String SERVER_ADDRESS = "localhost";
//    private static final int SERVER_PORT = 12345;
    private String serverAddress = "localhost";
    private int serverPort = 12345;
    private String userId; // 사용자 ID
    private String roomName;    // 방 이름

    private Socket socket;
    private ObjectOutputStream out;
    private DrawPanel drawPanel;    // 그림판 Panel
    private ChatingListPanel chatingListPanel; // 채팅창 Panel
    private LeftUserPanel leftUserPanel;
    private RightUserPanel rightUserPanel;
    private DrawingSetting drawingSetting;
    private InputPanel inputPanel;
    private boolean isDrawing = false;      // 그림 그리고 있는지 확인
    private Point lastPoint = null;  // 마지막 좌표
    // 지우개 사용중인지 확인
    private boolean isEraserOn = false;
    // 현재 플레이어가 준비완료인지
    private boolean isReady = false;
    private ObjectInputStream in;

    public DrawingClient(Socket socket, ObjectOutputStream out, ObjectInputStream in,String roomName, String userId, String serverAddress, int serverPort) {
        this.socket=socket;
        this.out=out;
        this.roomName = roomName;
        this.userId = userId;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.in = in;
        buildGUI(); // GUI 구성
        connectToServer(); // 서버에 접속요청
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
        inputPanel = new InputPanel(this);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(drawPanel, BorderLayout.CENTER);
        centerPanel.add(leftUserPanel, BorderLayout.WEST);
        centerPanel.add(rightUserPanel, BorderLayout.EAST);
        // 이 보더 레이아웃 East 영역을 다른 용도로 활용해보면 어떨까
        centerPanel.add(drawingSetting, BorderLayout.NORTH);

        centerPanel.add(inputPanel, BorderLayout.SOUTH);


        drawingSetting.getEraser().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isEraserOn) {
                    isEraserOn = false;
                    drawingSetting.getIsEraser().setText("지우개 사용중 X");
                }
                else {
                    isEraserOn = true;
                    drawingSetting.getIsEraser().setText("지우개 사용중");
                }
            }
        });

        drawingSetting.getReadyButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isReady) {
                    isReady = false;
                    // 준비를 취소하겠다고 서버에 송신
                    send(new SketchingData(SketchingData.MODE_INDIVIDUAL_READY, roomName, userId, isReady));
                }
                else {
                    isReady = true;
                    // 준비를 하겠다고 서버에 송신
                    send(new SketchingData(SketchingData.MODE_INDIVIDUAL_READY, roomName, userId, isReady));
                }
            }
        });


        add(centerPanel);
        //add(inputPanel, BorderLayout.SOUTH);
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
                Color selectedColor = isEraserOn ? Color.WHITE : drawingSetting.getSelectedColor();
                float selectedWidth = isEraserOn ? 20 : drawingSetting.getSelectedLineWidth();

                // Line 객체 생성 및 색깔, 굵기 설정
                Line line = new Line(lastPoint.x, lastPoint.y, e.getX(), e.getY(), selectedColor, selectedWidth);

                // SketchingData 객체 생성
                SketchingData sketchingData = new SketchingData(SketchingData.MODE_LINE, line, roomName);

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


//    //UserPanel에 사용자 추가하는 메서드
//    private void addUser(String userId) {
//        if (!leftUserPanel.addUser(userId, 0)) {
//            rightUserPanel.addUser(userId, 0);
//        }
//    }
//
//    //UserPanel에 사용자 제거하는 메서드
//    private void removeUser(String userId) {
//        if (!leftUserPanel.removeUser(userId)) {
//            rightUserPanel.removeUser(userId);
//        }
//    }

    private void connectToServer() {
        // Swing은 단일 스레드 환경임. EDT라는 스레드에서 혼자 UI 업데이트를 처리.
        // 이때, 서버에 접속하는 네트워크 작업을 EDT에서 수행하게되면 UI 업데이트가 블록되어짐. 즉 컴포넌트들이 렌더링되지 않음.
        // so, 별도의 스레드에서 네트워크 작업을 수행해야 함.
        new Thread(() -> {
            try {
                //socket = new Socket(serverAddress, serverPort);
                //out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                socket=this.socket;
                out=this.out;


                //out.flush();

                // 서버에 사용자 ID 전송//
                // connectToServer 메서드가 out 객체를 초기화하기 전에 sendUserID 메서드가 호출되면, out 객체가 null이므로 NullPointerException 발생.
                // 이를 방지하기 위해 sendUserID 메서드를 connectToServer 메서드 내부에서 호출하도록 변경.
//                sendUserID(); // drawingThread & sendCoordsThread 스레드 실행 전에 꼭 먼저 실행되어야 함. 그렇지않으면 채팅 및 그리기 동작 이후에서야 아이디 전송이 이루어지게 됨.

                //순서: 그리기를 실행하는 스레드 실행 후 -> 데이터 수신 스레드 실행. 역순으로하게되면 채팅 전송하기전까지 그림을 그릴 수 없음.
                DrawingThread drawingThread = new DrawingThread();
                drawingThread.start();

                Thread receiveThread = new ReceiveThread(socket);
                receiveThread.start(); //ObjectOutputStream을 ObjectInputStream보다 먼저 생성해야 함. 미준수시 데드락 발생 가능성 있음.

                // 서버에 접속한 사용자를 UserPanel에 추가
                //addUser(userId); // 클라이언트가 직접 추가하는게 아닌, 서버에서 현재 접속자들 받아오는 방식으로 변경.

            } catch (IOException e) {
                chatingListPanel.addMessage("서버에 연결할 수 없습니다: " + e.getMessage());
            }
        }).start();
    }

    // 메세지를 수신하는 스레드
    private class ReceiveThread extends Thread {


        public ReceiveThread(Socket socket) throws IOException {
           // in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        }

        @Override
        public void run() {


            while (true) {
                try {
                    SketchingData data = (SketchingData) in.readObject();

                    if (data == null) {// 서버측에서 소켓연결을 종료하여 스트림이 닫힌경우.
                        disconnect();
                        chatingListPanel.addMessage("서버 연결 끊김");
                        return;
                    }

                    // 서버로부터 받는 데이터의 방 이름이 현재 방 이름과 같은 경우만
                    if (roomName.equals(data.getRoomName()) || roomName.equals(data.getUserID())) {
                        switch (data.getMode()) { // 수신된 메시지의 모드값에 따라 다른 처리.
                            case SketchingData.MODE_CHAT: // 채팅모드라면, 서버로부터 전달받은 id 와 문자열 메시지를 화면에 출력.
                                if (data.getUserID().equals(userId)) {
                                    chatingListPanel.addMessage("나: " + data.getMessage());
                                } else {
                                    chatingListPanel.addMessage(data.getUserID() + ": " + data.getMessage());
                                }
                                System.out.println(data.getRoomName() + "에서 채팅");
                                break;

                            case SketchingData.MODE_LINE:  // 그리기 모드를 받았을 때
                                Line line = data.getLine();
                                drawPanel.addLine(line.getX1(), line.getY1(), line.getX2(), line.getY2(), line.getColor(), line.getLineWidth());
                                break;

                            case SketchingData.MODE_CLIENT_LIST:
                                Vector<String> userIDList = data.getuserIDList();
                                Vector<Integer> userScoreList = data.getuserScoreList();
                                System.out.println("userIDList size = " + userIDList.size());
                                for (String userID : userIDList) {
                                    System.out.println("userID: " + userID);
                                }
                                for (int score : userScoreList) {
                                    System.out.println("score: " + score);
                                }
                                updateUserPanel(userIDList, userScoreList);
                                break;

                            case SketchingData.MODE_INDIVIDUAL_READY:
                                // 준비완료 및 취소가 성공적으로 이뤄졌을 때
                                if (data.isSuccess()) {
                                    String userId = data.getUserID();
                                    boolean isReady = data.isReady();

                                    if (isReady) {
                                        chatingListPanel.addMessage(userId + "님이 준비완료하였습니다.");
                                    } else {
                                        chatingListPanel.addMessage(userId + "님이 준비를 취소하였습니다.");
                                    }
                                    break;
                                }
                                // 준비완료 및 취소가 실패했을 때 ex) 혼자 방에 있는데 준비완료 하는 경우
                                else {
                                    chatingListPanel.addMessage("2인 이상 있을 때만 준비완료 가능합니다.");
                                    isReady = false;
                                    break;
                                }
                            case SketchingData.GAME_START:
                                chatingListPanel.addMessage("게임이 시작되었습니다.");
                                break;
                        }
                    }
//                    else if (data.getMode() == SketchingData.MODE_LOGOUT) {
//                        removeUser(data.getUserID());
//                    }
                } catch (IOException e) {
                    chatingListPanel.addMessage("연결을 종료했습니다.");
                } catch (ClassNotFoundException e) {
                    chatingListPanel.addMessage("잘못된 객체가 전달되었습니다.");
                    throw new RuntimeException(e);

                }
            }
        }
    }

    private void updateUserPanel(Vector<String> userIDList, Vector<Integer> userScoreList) {

        // 로그아웃한 플레이어를 userPanel에 반영시키기위해 모든 패널 초기화
        for (int i = 0; i < 4; i++) {
            leftUserPanel.getUser(i).setUser("", 0);
            rightUserPanel.getUser(i).setUser("", 0);
        }

        for (int i = 0; i < userIDList.size(); i++) {
            if (i < 4) {
                leftUserPanel.getUser(i).setUser(userIDList.get(i), userScoreList.get(i));
            } else {
                rightUserPanel.getUser(i - 4).setUser(userIDList.get(i), userScoreList.get(i));
            }
        }
        leftUserPanel.repaint();
        rightUserPanel.repaint();
    }


    void disconnect() {
        send(new SketchingData(SketchingData.MODE_LOGOUT, userId));
        try {
            socket.close();  //???? 왜 무한출력되지?
            System.exit(-1); //????
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
    private void send(SketchingData data) {
        try {
            out.writeObject(data);
            out.flush();
        } catch (IOException e) {
            System.err.println("클라이언트 일반 전송 오류> " + e.getMessage());
            System.exit(0);
        }
    }

    void sendMessage(String message) { // 행단위의 문자열 전송 대신에, send() 메서드를 통해서 하나의 채팅메시지인 ChatMsg 객체를 전송하는 방식으로 변경.

        //chatingListPanel.addMessage("나: " + message); 추후 서버로부터 브로드케스팅받고 -> 그 메시지모드가 채팅이고 + userId가 나랑 같다면 => 채팅리스트에 "나: "로 추가하는 방식으로 변경.
        if (message.isEmpty())
            return;

        send(new SketchingData(SketchingData.MODE_CHAT, userId, message, roomName));
        // ChatMsg 객체로 만들어서 전송.
    }

//
//    public static void main(String[] args) {
//        String userId = JOptionPane.showInputDialog("사용자 ID를 입력하세요:"); // 다이얼로그로 사용자 ID 입력
//        DrawingClient drawingClient = new DrawingClient(userId);
//        //drawingClient.connectToServer();
//    }
}