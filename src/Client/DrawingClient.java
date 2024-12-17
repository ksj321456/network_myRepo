package Client;

import etc.BgmManager;
import etc.ChatType;
import etc.Line;
import etc.SketchingData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.SortedMap;
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
    private BottomPanel bottomPanel;

    private boolean isDrawing = false;      // 그림 그리고 있는지 확인
    private Point lastPoint = null;  // 마지막 좌표
    // 지우개 사용중인지 확인
    private boolean isEraserOn = false;
    // 현재 플레이어가 준비완료인지
    private boolean isReady = false;
    private ObjectInputStream in;

    // 현재 그림을 그릴 수 있는 상태인지
    private boolean canDrawing = false;
    private CountDownBar countDownBar;

    private String prevPainter = ""; // 이전 라운드의 화가 식별을 위한 변수

    public DrawingClient(Socket socket, ObjectOutputStream out, ObjectInputStream in, String roomName, String userId, String serverAddress, int serverPort) {
        this.socket = socket;
        this.out = out;
        this.roomName = roomName;
        this.userId = userId;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.in = in;
        buildGUI(); // GUI 구성
        connectToServer(); // 서버에 접속요청

        BgmManager.loopAudio(4);
    }

    private void buildGUI() {
        setResizable(false);
        setTitle("Hansung Sketch " + roomName);
        setSize(1400, 800);
        setLocationRelativeTo(null); // 화면 중앙에 프레임 띄우기
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //DO_NOTHING_ON_CLOSE로,프레임 윈도우창 X 버튼 클릭시 아무동작 안하게 설정 & 커스텀 동작 설정
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 윈도우 이벤트 리스너 추가
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { // 창 닫기 버튼 클릭 시
                // 창 닫기 확인 다이얼로그 표시 등의 작업 수행
                int result = JOptionPane.showConfirmDialog(
                        DrawingClient.this,
                        "한성스케치를 종료하시겠습니까?",
                        "게임 종료",
                        JOptionPane.YES_NO_OPTION
                );

                if (result == JOptionPane.YES_OPTION) {
                    disconnect(); // 연결 종료
                    System.exit(0); // 프로그램 종료 -> 자원 해제
                }
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout());
        drawPanel = new DrawPanel();
        leftUserPanel = new LeftUserPanel(userId);
        rightUserPanel = new RightUserPanel(userId);
        chatingListPanel = new ChatingListPanel();
        countDownBar = new CountDownBar(60, this); // 카운트다운바 생성(인자값: 카운트다운 시간 1분)
        drawingSetting = new DrawingSetting(countDownBar);
        inputPanel = new InputPanel(this);
        bottomPanel = new BottomPanel(this);


        drawingSetting.getEraser().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isEraserOn) {
                    isEraserOn = false;
                    drawingSetting.getIsEraser().setText("지우개 사용중 X");
                } else {
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
                } else {
                    isReady = true;
                    // 준비를 하겠다고 서버에 송신
                    send(new SketchingData(SketchingData.MODE_INDIVIDUAL_READY, roomName, userId, isReady));
                }
            }
        });


        centerPanel.add(drawPanel, BorderLayout.CENTER);
        centerPanel.add(leftUserPanel, BorderLayout.WEST);
        centerPanel.add(rightUserPanel, BorderLayout.EAST);
        // 이 보더 레이아웃 East 영역을 다른 용도로 활용해보면 어떨까
        centerPanel.add(drawingSetting, BorderLayout.NORTH);

        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(centerPanel);

        chatingListPanel.add(inputPanel, BorderLayout.SOUTH);
        add(chatingListPanel, BorderLayout.EAST);

        drawPanel.setEnabled(false);

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
            if (canDrawing) {
                if (isDrawing && lastPoint != null) { // 그리기 상태일 때만 좌표를 전송
                    Color selectedColor = isEraserOn ? Color.WHITE : drawingSetting.getSelectedColor();
                    float selectedWidth = isEraserOn ? 20 : drawingSetting.getSelectedLineWidth();

                    // etc.Line 객체 생성 및 색깔, 굵기 설정
                    Line line = new Line(lastPoint.x, lastPoint.y, e.getX(), e.getY(), selectedColor, selectedWidth);

                    // etc.SketchingData 객체 생성
                    SketchingData sketchingData = new SketchingData(SketchingData.MODE_LINE, line, roomName);

                    // 서버에 etc.Line 객체 전송
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
            if (canDrawing) {
                isDrawing = true; // 그리기 시작
                lastPoint = new Point(e.getX(), e.getY()); // 현재 좌표 저장
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (canDrawing) {
                isDrawing = false; // 그리기 상태 종료
            }
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
                chatingListPanel.addMessage("서버에 연결할 수 없습니다: " + e.getMessage(), ChatType.SYSTEM_MESSAGE);
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
                        chatingListPanel.addMessage("서버 연결 끊김", ChatType.SYSTEM_MESSAGE);
                        return;
                    }

                    // 서버로부터 받는 데이터의 방 이름이 현재 방 이름과 같은 경우만
                    if (roomName.equals(data.getRoomName()) || roomName.equals(data.getUserID())) {
                        switch (data.getMode()) { // 수신된 메시지의 모드값에 따라 다른 처리.
                            case SketchingData.MODE_CHAT: // 채팅모드라면, 서버로부터 전달받은 id 와 문자열 메시지를 화면에 출력.
                                if (data.getUserID().equals(userId)) {
                                    chatingListPanel.addMessage(data.getMessage(), ChatType.MY_CHAT);
                                } else {
                                    chatingListPanel.addMessage(data.getUserID() + ": " + data.getMessage(), ChatType.OTHERS_CHAT);
                                }
                                System.out.println(data.getRoomName() + "에서 채팅");
                                break;

                            case SketchingData.MODE_LINE:  // 그리기 모드를 받았을 때
                                Line line = data.getLine();
                                drawPanel.addLine(line.getX1(), line.getY1(), line.getX2(), line.getY2(), line.getColor(), line.getLineWidth());
                                // 받은 좌표값을 하당 coordinates JPanel에 출력
                                String showCoords = String.format("좌표값: (%d, %d)", line.getX2(), line.getY2());
                                bottomPanel.getCoordinates().setText(showCoords);
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
                                        chatingListPanel.addMessage(userId + "님이 준비완료하였습니다.", ChatType.SYSTEM_MESSAGE);
                                    } else {
                                        chatingListPanel.addMessage(userId + "님이 준비를 취소하였습니다.", ChatType.SYSTEM_MESSAGE);
                                    }
                                    break;
                                }
                                // 준비완료 및 취소가 실패했을 때 ex) 혼자 방에 있는데 준비완료 하는 경우
                                else {
                                    chatingListPanel.addMessage("2인 이상 있을 때만 준비완료 가능합니다.", ChatType.SYSTEM_MESSAGE);
                                    isReady = false;
                                    break;
                                }
                            case SketchingData.GAME_START:
                                chatingListPanel.setAfterStartFont(); // 게임 시작 후 제시어전용 폰트로 변경
                                chatingListPanel.addMessage("게임이 시작되었습니다.", ChatType.SYSTEM_MESSAGE);

                                break;
                            case SketchingData.ROUND_START:
                                // 새로운 JLabel 생성 및 이미지 설정
                                JLabel imageLabel = new JLabel(new ImageIcon("images/letsPlay.png"));
                                // JLabel 크기 설정 (이미지 크기와 동일하게)
                                imageLabel.setSize(300, 300);

                                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

                                int x = (drawPanel.getWidth() - imageLabel.getWidth()) / 2;
                                int y = (drawPanel.getHeight() - imageLabel.getHeight()) / 2;
                                imageLabel.setLocation(x, y); // 위치 설정
                                drawPanel.add(imageLabel); // drawPanel에 추가
                                drawPanel.repaint();
                                // 5초 후 이미지 레이블 제거
                                Timer timer = new Timer(5000, e -> {
                                    drawPanel.remove(imageLabel);
                                    drawPanel.repaint();
                                    // 이미지 레이블 제거 후 동작 실행
                                    countDownBar.start(); // 카운트다운 시작

                                    // 제시어
                                    String word = data.getMessage();
                                    // 화가
                                    String painter = data.getRoomName();
                                    chatingListPanel.addMessage(painter + "님이 화가입니다.", ChatType.SYSTEM_MESSAGE);

                                    // 화가만 그림을 그릴 수 있음, 화가에게만 제시어 표시
                                    if (userId.equals(painter)) {
                                        drawPanel.setEnabled(true);
                                        chatingListPanel.setWord("제시어: ", word);
                                        canDrawing = true;
                                        if (!userId.equals(prevPainter)) { // 중복 호출 방지: 이전 라운드의 화가가 아닌 경우
                                            inputPanel.getT_input().setEnabled(false); // 화가는 채팅을 못함
                                            inputPanel.getB_send().setEnabled(false);
                                        }
                                    } else {// 화가가 아닌 사람들은 제시어 ???로 표시
                                        drawPanel.setEnabled(false); // drawPanel 비활성화
                                        canDrawing = false; // 그림 그리기 불가능 상태로 변경
                                        if (userId.equals(prevPainter)) { // 중복 호출 방지: 이전 라운드의 화가였던 경우
                                            inputPanel.getT_input().setEnabled(true); // 화가가 아닌 사람은 채팅 가능
                                            inputPanel.getB_send().setEnabled(true);
                                        }
                                        StringBuilder maskedWord = new StringBuilder();
                                        for (int i = 0; i < word.length(); i++) {
                                            maskedWord.append("?");
                                        }
                                        chatingListPanel.setWord("제시어: ", maskedWord.toString());
                                    }
                                    prevPainter = painter; // 현재 라운드의 화가 정보 저장
                                });
                                timer.setRepeats(false);
                                timer.start();

                                break;
                            // 정답을 맞춘 사람이 나타났을 때
                            case SketchingData.MODE_CORRECT:
                                BgmManager.playAudio(6);
                                chatingListPanel.addMessage(data.getUserID() + "님이 정답을 맞췄습니다.", ChatType.SYSTEM_MESSAGE);
                                drawPanel.clear();
                                countDownBar.stop(); // 카운트다운 멈춤
                                Vector<String> userIdList = data.getuserIDList();
                                Vector<Integer> userscoreList = data.getuserScoreList();
                                updateUserPanel(userIdList, userscoreList);

                                // 정답자 표시 JLabel 생성
                                JLabel correctLabel = new JLabel(data.getUserID() + "님이 정답을 맞추셨습니다!");
                                correctLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20)); // 폰트 설정
                                correctLabel.setForeground(Color.RED); // 색상 설정
                                correctLabel.setSize(500, 50); // 크기 설정
                                correctLabel.setHorizontalAlignment(SwingConstants.CENTER);

                                int correctLabelX = (drawPanel.getWidth() - correctLabel.getWidth()) / 2;
                                int correctLabelY = 50;
                                correctLabel.setLocation(correctLabelX, correctLabelY);
                                drawPanel.add(correctLabel); // drawPanel에 추가

                                // 3초 딜레이 추가
                                Timer correctTimer = new Timer(3000, e -> {
                                    drawPanel.remove(correctLabel); // JLabel 제거
                                    drawPanel.repaint(); // drawPanel 다시 그리기
                                });
                                correctTimer.setRepeats(false);
                                correctTimer.start();
                                break;

                            case SketchingData.MODE_NOBODY_CORRECT:
                                BgmManager.playAudio(3);
                                drawPanel.clear();
                                countDownBar.stop(); // 카운트다운 멈춤


                                JLabel imageLabel2 = new JLabel(new ImageIcon("images/areYouReady.png"));
                                // JLabel 크기 설정 (이미지 크기와 동일하게)
                                imageLabel2.setSize(300, 300);

                                imageLabel2.setHorizontalAlignment(SwingConstants.CENTER);

                                int x2 = (drawPanel.getWidth() - imageLabel2.getWidth()) / 2;
                                int y2 = (drawPanel.getHeight() - imageLabel2.getHeight()) / 2;
                                imageLabel2.setLocation(x2, y2); // 위치 설정
                                drawPanel.add(imageLabel2); // drawPanel에 추가


                                // 정답자 표시 JLabel 생성
                                JLabel nobodyLabel = new JLabel("아무도 정답을 맞추지 못했습니다! 정답은 <" + data.getPrevWord() + "> 입니다!");
                                nobodyLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15)); // 폰트 설정
                                nobodyLabel.setForeground(Color.RED); // 색상 설정
                                nobodyLabel.setSize(500, 50); // 크기 설정
                                nobodyLabel.setHorizontalAlignment(SwingConstants.CENTER);

                                int nobodyLabelX = (drawPanel.getWidth() - nobodyLabel.getWidth()) / 2;
                                int nobodyLabelY = 50;
                                nobodyLabel.setLocation(nobodyLabelX, nobodyLabelY);
                                drawPanel.add(nobodyLabel); // drawPanel에 추가
                                drawPanel.repaint();
                                String painter = data.getRoomName();
                                chatingListPanel.addMessage("아무도 정답을 맞추지 못했습니다!", ChatType.SYSTEM_MESSAGE);
                                String msg = String.format("정답은 <%s> 입니다!", data.getPrevWord());
                                chatingListPanel.addMessage(msg, ChatType.ANSWER_MESSAGE);
                                chatingListPanel.addMessage(painter + "님이 다시 화가로 플레이합니다.", ChatType.SYSTEM_MESSAGE);

                                // 5초 딜레이 후 imageLabel2 제거
                                Timer nobodyTimer2 = new Timer(5000, e -> {
                                    drawPanel.remove(imageLabel2);
                                    drawPanel.repaint(); // drawPanel 다시 그리기
                                });
                                nobodyTimer2.setRepeats(false);
                                nobodyTimer2.start();

                                // 3초 딜레이 추가
                                Timer nobodyTimer = new Timer(3000, e -> {
                                    drawPanel.remove(nobodyLabel); // JLabel 제거

                                    drawPanel.repaint(); // drawPanel 다시 그리기
                                    // 이미지 레이블 제거 후 동작 실행
                                    countDownBar.start(); // 카운트다운 시작

                                    // 제시어
                                    String word = data.getMessage();
                                    // 화가


                                    // 화가만 그림을 그릴 수 있음, 화가에게만 제시어 표시
                                    if (userId.equals(painter)) {
                                        drawPanel.setEnabled(true);
                                        chatingListPanel.setWord("제시어: ", word);
                                        canDrawing = true;
                                        if (!userId.equals(prevPainter)) { // 중복 호출 방지: 이전 라운드의 화가가 아닌 경우
                                            inputPanel.getT_input().setEnabled(false); // 화가는 채팅을 못함
                                            inputPanel.getB_send().setEnabled(false);
                                        }
                                    } else {// 화가가 아닌 사람들은 제시어 ???로 표시
                                        drawPanel.setEnabled(false); // drawPanel 비활성화
                                        canDrawing = false; // 그림 그리기 불가능 상태로 변경
                                        if (userId.equals(prevPainter)) { // 중복 호출 방지: 이전 라운드의 화가였던 경우
                                            inputPanel.getT_input().setEnabled(true); // 화가가 아닌 사람은 채팅 가능
                                            inputPanel.getB_send().setEnabled(true);
                                        }
                                        StringBuilder maskedWord = new StringBuilder();
                                        for (int i = 0; i < word.length(); i++) {
                                            maskedWord.append("?");
                                        }
                                        chatingListPanel.setWord("제시어: ", maskedWord.toString());
                                    }
                                    prevPainter = painter; // 현재 라운드의 화가 정보 저장
                                });
                                nobodyTimer.setRepeats(false);
                                nobodyTimer.start();
                                break;

                            // 플레이어 입장시 입장 bgm 재생
                            case SketchingData.MODE_ENTERSOUND:
                                BgmManager.playAudio(5);
                                break;

                            case SketchingData.GAME_OVER:
                                countDownBar.stop(); // 게임 종료 시 카운트다운 멈춤
                                drawPanel.clear();
                                chatingListPanel.addMessage(data.getUserID() + "님이 50점을 달성하여 게임을 종료합니다.", ChatType.WINNER);

                                Vector<String> useridList = data.getuserIDList();
                                Vector<Integer> userscorelist = data.getuserScoreList();
                                System.out.println("userIdList의 크기: " + useridList.size());
                                updateUserPanel(useridList, userscorelist);

                                // 점수를 Key, userID를 value로 하는 SortedMap으로 역정렬 상태 관리
                                SortedMap<String, Integer> sortedMap = data.getSortedMap();

                                int idx = 1;
                                StringBuilder sb = new StringBuilder();
                                for (String key : sortedMap.keySet()) {
                                    String[] parts = key.split("_");
                                    int score = Integer.parseInt(parts[0]);
                                    String userId = parts[1];
                                    sb.append(String.format("%d등     %s      점수: %d\n", idx++, userId, score));
                                }
                                BgmManager.playAudio(0);
                                JOptionPane.showMessageDialog(null, sb.toString());


                                // 게임을 종료 후 다시 그림을 못 그리게 설정
                                canDrawing = false;
                                // 준비완료 false
                                isReady = false;

                                // Painter 클라이언트의 채팅 버튼과 텍스트 필드 활성화
                                inputPanel.getT_input().setEnabled(true);
                                inputPanel.getB_send().setEnabled(true);

                                prevPainter = ""; // 이전 라운드의 화가 정보 초기화
                                break;
                        }
                    }
//                    else if (data.getMode() == etc.SketchingData.MODE_LOGOUT) {
//                        removeUser(data.getUserID());
//                    }
                } catch (IOException e) {
                    chatingListPanel.addMessage("연결을 종료했습니다.", ChatType.SYSTEM_MESSAGE);
                } catch (ClassNotFoundException e) {
                    chatingListPanel.addMessage("잘못된 객체가 전달되었습니다.", ChatType.SYSTEM_MESSAGE);
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


    public void disconnect() {
        send(new SketchingData(SketchingData.MODE_LOGOUT, userId));
        BgmManager.stopAudio(4);
        BgmManager.setVolume(4, 0.1f);
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }

            //System.exit(-1); // 프로그램 종료
        } catch (IOException e) {
            System.err.println("클라이언트 닫기 오류> " + e.getMessage());
            // System.exit(-1);
        } finally {
            dispose(); // Client.DrawingClient 프레임창 닫기 & 자원해제
        }
    }


    //    public void sendMessage(String message) {
//        String fullMessage = userId + ": " + message;
//        etc.SketchingData chatData = new etc.SketchingData(etc.SketchingData.MODE_CHAT, userId, fullMessage);
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


    public void nobodyCorrect() {
        if (canDrawing)  // 화가일 때만 메시지 전송
            send(new SketchingData(SketchingData.MODE_NOBODY_CORRECT, userId, "", roomName));
    }


//
//    public static void main(String[] args) {
//        String userId = JOptionPane.showInputDialog("사용자 ID를 입력하세요:"); // 다이얼로그로 사용자 ID 입력
//        Client.DrawingClient drawingClient = new Client.DrawingClient(userId);
//        //drawingClient.connectToServer();
//    }
}