import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.List;

public class DrawingServer extends JFrame {
    private static final int PORT = 12345;
    private ServerSocket serverSocket = null;
    private Thread acceptThread = null;

    // 게임에 접속한 모든 클라이언트들을 저장하는 벡터(특정 게임방에 있는 클라이언트들을 저장하는 벡터가 아님. 모든 방의 클라이언트들을 저장)
    private Vector<ClientHandler> clients = new Vector<>();
    private JTextArea t_display;
    private JButton b_connect, b_disconnect, b_exit;

    //각 게임방별로 유저들을 관리하는 맵(특정 게임방에 있는 클라이언트들을 저장하는 hashMap)
    private Map<String, Map<String, Integer>> rooms = new HashMap<>();

    // key => 방 이름, value => 해당 방에 준비한 플레이어 수
    private Map<String, Integer> roomReadyCnt = new HashMap<>();

    // 현재 해당 게임방이 게임 중인지 아닌지 확인
    private Map<String, Boolean> isGameMap = new HashMap<>();

    // 현재 해당 게임중인 방의 제시어
    private Map<String, String> wordMap = new HashMap<>();

    public DrawingServer() {
        setTitle("Hansung Sketch Server");

        /* 프레임의 위치를 화면 중앙으로 설정하는 절차들 */
        // 현재 사용자의 모니터 화면의 크기를 가져옴
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // 화면의 가로세로 중앙 계산
        int centerX = (int) (screenSize.getWidth() - 550) / 2;
        int centerY = (int) (screenSize.getHeight() - 600) / 2;
        setBounds(centerX, centerY, 1000, 500);
        /*-------------------------------------*/

        buildGUI();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private void buildGUI() {
        add(createDisplayPanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    // 디스플레이 패널
    private JPanel createDisplayPanel() {
        JPanel dispalyPanel = new JPanel(new BorderLayout());
        t_display = new JTextArea();
        JScrollPane scroll = new JScrollPane(t_display);
        t_display.setEditable(false);
        dispalyPanel.add(scroll);

        return dispalyPanel;
    }

    // control 패널
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 0));
        b_connect = new JButton("서버 시작");
        b_disconnect = new JButton("서버 종료");
        b_disconnect.setEnabled(false);
        b_exit = new JButton("종료");

        b_connect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                acceptThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        startServer(); // 작업 스레드에서 서버 시작.
                    }
                });
                acceptThread.start();

                b_connect.setEnabled(false);
                b_exit.setEnabled(false);
                b_disconnect.setEnabled(true);
            }
        });

        b_disconnect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                disconnect(); // 접속끊기버튼 클릭시 서버와 연결종료.
            }
        });

        b_exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // 프로그램 정상 종료.
            }
        });
        controlPanel.add(b_connect);
        controlPanel.add(b_disconnect);
        controlPanel.add(b_exit);

        b_exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {// 종료버튼이 눌려지면 서버소켓을 닫아준다. 이로써 프로그램 종료.
                try {
                    if (serverSocket != null)
                        serverSocket.close();
                } catch (IOException e) {
                    System.err.println("서버닫기 오류> " + e.getMessage());
                }
            }
        });

        controlPanel.add(b_exit, BorderLayout.CENTER);

        return controlPanel;
    }


    public void startServer() {
        Socket clientSocket = null;
        InetAddress inetAddress = null;
        try {

            serverSocket = new ServerSocket(PORT);// 해당 포트와 연결된 서버소켓 객체 생성.
            inetAddress = InetAddress.getLocalHost();
            printDisplay("서버가 시작되었습니다: " + inetAddress.getHostAddress() + "\n");

            while (acceptThread == Thread.currentThread()) {
                clientSocket = serverSocket.accept();

                String cAddress = clientSocket.getInetAddress().getHostAddress();
                printDisplay("클라이언트가 연결되었습니다: " + cAddress);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (SocketException e) {
            printDisplay("서버 소켓 종료");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null)
                    clientSocket.close();
                if (serverSocket != null)
                    serverSocket.close();

            } catch (IOException e) {
                System.err.println("서버닫기 오류> " + e.getMessage());
                System.exit(-1);
            }
        }
    }

    private class ClientHandler extends Thread {
        private final Socket clientSocket;
        private ObjectOutputStream out;
        private String userID; // 현재 서버측 각 클라이언트 소켓들이 연결중인 대응중인 사용자의 아이디
        private int score; // 현재 이 사용자의 점수

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.score = 0;
        }

        @Override
        public void run() {
            String message;
            SketchingData data = null;
            try {
                //ObjectOutputStream을 ObjectInputStream보다 먼저 생성해야 함. 순서 바뀌면 데드락 발생.
                //현재 이 소켓이, ObjectInputStream을 생성하기 전에 헤더 정보를 수신하면 ObjectInputStream은 스트림 헤더를 기대하지 않았기 때문에 블록 상태에 빠지기때문.
                out = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                //out.flush();
                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));


                while ((data = (SketchingData) in.readObject()) != null) {
                    //스케치 데이터를 받았을 때
                    //printDisplay("클라이언트로부터 데이터 수신");
                    if (data.getMode() == SketchingData.MODE_LOGIN) { // 읽어온 메시지의 모드값이 로그인 메시지라면
                        userID = data.getUserID(); // uid에 로그인한 클라이언트의 아이디를 저장.
//                        sendPlayerList(); // 각 클라이언트에게 현재 접속중인 플레이어 리스트 전송
                        printDisplay("NEW 플레이어: " + userID);
                        printDisplay("현재 접속중인 플레이어 수: " + clients.size() + currentPlayers());
                        broadcast(new SketchingData(SketchingData.MODE_LOGIN, userID));

                        // 로그인시 방이 하나라도 있다면 새로운 클라이언트는 존재하는 방들을 확인해야 한다.
                        if (!rooms.isEmpty()) {
                            // 현재 있는 방의 이름들을 roomNames Vector에 저장
                            Vector<String> roomNames = new Vector<>();
                            for (String roomName : rooms.keySet()) {
                                roomNames.add(roomName);
                            }
                            // 존재하는 방들의 이름 전송
                            broadcast(new SketchingData(SketchingData.SHOW_ROOM_LIST, roomNames, data.getUserID()));
                        }

                        continue;
                    } else if (data.getMode() == SketchingData.MODE_LOGOUT) { // 로그아웃 메시지라면
                        break; // 클라이언트측과의 연결을 해제
                    }                    //채팅 메시지를 받았을 때
                    else if (data.getMode() == SketchingData.MODE_CHAT) {
                        printDisplay("[채팅 메시지]" + userID + ": " + data.getMessage());
                        broadcast(data);

                        // 해당 방이 게임 중이고 정답을 맞췄을 경우
                        if (isGameMap.get(data.getRoomName())) {
                            if (data.getMessage().equals(wordMap.get(data.getRoomName()))) {
                                printDisplay(data.getRoomName() + " 방에서 " + data.getUserID() + "님이 정답을 맞췄습니다. -> 10점 추가");

                                Vector<String> userIDList = new Vector<>();
                                Vector<Integer> userScoreList = new Vector<>();

                                // 정답을 맞춘 플레이어에게 10점을 추가해도 50점이 안 될 때
                                if (rooms.get(data.getRoomName()).get(data.getUserID()) + 10 != 50) {
                                    // 정답자에게 10점 추가
                                    int newScore = rooms.get(data.getRoomName()).get(data.getUserID()) + 10;
                                    Map<String, Integer> map = rooms.get(data.getRoomName());
                                    map.put(data.getUserID(), newScore);
                                    rooms.put(data.getRoomName(), map);

                                    for (String userId : map.keySet()) {
                                        userIDList.add(userId);
                                        userScoreList.add(map.get(userId));
                                    }
                                    // 클라이언트에게 누가 정답을 맞췄는지 통보
                                    broadcast(new SketchingData(SketchingData.MODE_CORRECT, data.getRoomName(), data.getUserID(), userIDList, userScoreList));

                                    // 새로운 라운드 시작
                                    String word = WordList.getWord();

                                    // painter => 정답을 맞춘 사람이 화가가 됨
                                    String painter = data.getUserID();

                                    // 클라이언트에 모드값, 방 이름, 제시어, 화가가 될 클라이언트 랜덤으로 선정 후 전송
                                    broadcast(new SketchingData(SketchingData.ROUND_START, data.getRoomName(), word, painter));
                                    // 해당 게임 방은 게임 중으로 설정
                                    isGameMap.put(data.getRoomName(), true);
                                    wordMap.put(data.getRoomName(),word);
                                    printDisplay(data.getRoomName() + " 방에서 라운드가 시작됩니다.");
                                }
                                // 정답을 맞춘 플레이어가 50점을 달성하였을 경우
                                else {
                                    printDisplay(data.getRoomName() + " 방에서 우승자가 나왔습니다. ** " + data.getUserID() + " **");

                                    String winner = data.getUserID();

                                    // 기존에 접속한 플레이어들의 준비 상태 해제
                                    roomReadyCnt.put(data.getRoomName(), 0);

                                    // 플레이어들의 점수 0점으로 업데이트
                                    Map<String, Integer> map = rooms.get(data.getRoomName());
                                    Vector<String> userIdList = new Vector<>();
                                    Vector<Integer> userscoreList = new Vector<>();
                                    for (String userId : map.keySet()) {
                                        map.put(userId, 0);
                                    }

                                    // 업데이트된 Vector를 클라이언트에 전송

                                    for (String userId : map.keySet()) {
                                        userIdList.add(userId);
                                        userscoreList.add(map.get(userId));
                                    }

                                    broadcast(new SketchingData(SketchingData.GAME_OVER, data.getRoomName(), winner, userIdList, userscoreList));

                                    printDisplay(data.getUserID() + " 방의 게임을 종료합니다.");
                                }
                            }
                        }

                    } else if (data.getMode() == SketchingData.MODE_LINE) {
                        Line line = data.getLine();
                        //printDisplay("그리기 좌표: " + line.getX1() + ", " + line.getY1() + ", " + line.getX2() + ", " + line.getY2());
                        broadcast(data);
                    } else if (data.getMode() == SketchingData.CREATE_ROOM) {
                        // 방 생성 요청을 받았을 때

                        // 생성하고자하는 방의 이름이 중복된게 없을 때
                        if (!rooms.containsKey(data.getRoomName())) {
                            Map<String, Integer> map = new HashMap<>();
                            map.put(data.getOwnerName(), 0);
                            // key => 방 이름, value => key: userId, value: score
                            rooms.put(data.getRoomName(), map);
                            printDisplay("방 생성 (방 이름: " + data.getRoomName() + ") 방 갯수: " + rooms.size());
                            // 현재 게임 방은 게임 중이 아님
                            isGameMap.put(data.getRoomName(), false);
                            broadcast(new SketchingData(SketchingData.CREATE_ROOM, data.getRoomName(), data.getOwnerName(), data.getIPAddress(), data.getPortNumber()));
                            sendPlayerList();
                        }
                        // 방을 만든 직후이므로 해당 방의 준비 플레이어 수 = 0
                        roomReadyCnt.put(data.getRoomName(), 0);
                    }
                    // 방에 입장할 때
                    else if (data.getMode() == SketchingData.ENTER_ROOM) {
                        // 전달받은 roomName 속성을 통해 방을 찾고 해당 DrawingClient 불러오기
                        // 입장하고자 하는 방의 이름을 받아 value인 Map 업데이트 한 후 put

                        // 방의 이름을 받아서 Map 객체를 꺼내와 put으로 업데이트
                        rooms.get(data.getRoomName()).put(data.getOwnerName(), 0);
                        for (String roomName : rooms.keySet()) {
                            if (roomName.equals(data.getRoomName())) {
                                broadcast(new SketchingData(data.getMode(), data.getRoomName(), data.getOwnerName(), data.getIPAddress(), data.getPortNumber()));
                                sendPlayerList();
                            }
                        }
                    }
                    // 준비를 하거나 취소할 때의 로직
                    else if (data.getMode() == SketchingData.MODE_INDIVIDUAL_READY) {
                        // 준비를 하고자 할 때
                        if (data.isReady()) {

                            // 혼자 있을 때는 게임 플레이가 불가능 즉, 혼자 방에 있을 때는 준비 불가
                            if (rooms.get(data.getRoomName()).size() == 1) {
                                broadcast(new SketchingData(data.getMode(), data.getRoomName(), data.getUserID(), data.isReady(), false));
                                printDisplay(data.getUserID() + " 준비 실패, 2인 이상부터 준비 완료 가능.");
                                continue;
                            }

                            int cnt = roomReadyCnt.get(data.getRoomName());

                            cnt += 1;
                            roomReadyCnt.put(data.getRoomName(), cnt);
                            // 만약 cnt가 2 이상이고 현재 접속해있는 인원수와 같다면 게임 시작
                            if (cnt >= 2 && rooms.get(data.getRoomName()).size() == cnt) {
                                // 우선 플레이어가 준비완료되었음을 알려줌
                                broadcast(new SketchingData(data.getMode(), data.getRoomName(), data.getUserID(), data.isReady(), true));
                                printDisplay(data.getRoomName() + " 방에서 " + data.getUserID() + " 준비 완료");
                                // 게임 시작을 클라이언트들에게 통지
                                broadcast(new SketchingData(SketchingData.GAME_START, data.getRoomName()));
                                printDisplay(data.getRoomName() + " 에서 게임이 시작되었습니다.");

                                // 바로 라운드 시작
                                // word => 제시어
                                String word = WordList.getWord();

                                Map<String, Integer> map = rooms.get(data.getRoomName());
                                List<String> list = new ArrayList<>();
                                for (String userId : map.keySet()) {
                                    list.add(userId);
                                }
                                Random random = new Random();
                                int idx = random.nextInt(list.size());

                                // painter => 랜덤으로 선정한 화가
                                String painter = list.get(idx);

                                // 클라이언트에 모드값, 방 이름, 제시어, 화가가 될 클라이언트 랜덤으로 선정 후 전송
                                broadcast(new SketchingData(SketchingData.ROUND_START, data.getRoomName(), word, painter));
                                // 해당 게임 방은 게임 중으로 설정
                                isGameMap.put(data.getRoomName(), true);
                                wordMap.put(data.getRoomName(),word);
                                printDisplay(data.getRoomName() + " 방에서 라운드가 시작됩니다.");
                            }
                            // 그렇지 않다면 단순히 클라이언트에 준비완료 사실 전송
                            else {
                                printDisplay(data.getRoomName() + " 방에서 " + data.getUserID() + " 준비 완료");
                                broadcast(new SketchingData(data.getMode(), data.getRoomName(), data.getUserID(), data.isReady(), true));
                            }
                        }
                        // 준비를 취소하고자 할 때
                        else {
                            // 준비 인원수에서 -1
                            int cnt = roomReadyCnt.get(data.getRoomName());
                            cnt -= 1;
                            roomReadyCnt.put(data.getRoomName(), cnt);

                            broadcast(new SketchingData(data.getMode(), data.getRoomName(), data.getUserID(), data.isReady(), true));

                            printDisplay(data.getRoomName() + " 방에서 " + data.getUserID() + " 준비 취소");
                        }
                    }
                }

                // 기존 알고리즘의 문제점: clients.remove(this); 를 통해 모든 방의 사용자관리 벡터에서 해당 사용자를 제거해주었으나, 정작 게임방 관리 맵에서는 제거해주지 않았음
                // 클라이언트 소켓이 종료될 때 rooms 맵에서 해당 게임방의 해당 사용자 제거
                for (String roomName : rooms.keySet()) {
                    Map<String, Integer> room = rooms.get(roomName);
                    if (room.containsKey(userID)) {
                        room.remove(userID);
                        // 방에 사용자가 없으면 방 자체를 제거
                        if (room.isEmpty()) {
                            rooms.remove(roomName);
                        }
                        break;
                    }
                }
///////////////////////////

                //while문을 빠져나왔다는 것은 클라이언트와의 연결이 끊어졌다는 뜻.
                clients.remove(this); // 연결이 끊은 클라이언트를 사용자벡터에서 제거. 현재 작업스레드를 벡터에서 제거.
                sendPlayerList(); // 한 플레이어가 퇴장했으므로, 플레이어 리스트를 갱신하여 모든 클라이언트에게 전송.
                printDisplay("플레이어 <" + userID + ">님이 퇴장하였습니다. 현재 참가자 수 : " + clients.size() + currentPlayers());
            } catch (IOException e) {
                clients.remove(this);
                sendPlayerList(); // 한 플레이어가 퇴장했으므로, 플레이어 리스트를 갱신하여 모든 클라이언트에게 전송.
                printDisplay(userID + " 연결 끊김. 현재 참가자 수 : " + clients.size());
            } catch (ClassNotFoundException e) {
                System.err.println("객체 전달 오류> " + e.getMessage());
            } finally {
                try {
/*                    // 클라이언트 소켓이 종료될 때 MODE_LOGOUT 메시지 처리
                    SketchingData logoutData = new SketchingData(SketchingData.MODE_LOGOUT, data.getUserID(), clientSocket.getInetAddress().toString());
                    broadcastOthers(logoutData, this);*/
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("서버 닫기 오류> " + e.getMessage());
                    System.exit(-1);
                }
            }
        }

        private void sendPlayerList() {
            for (String roomName : rooms.keySet()) {
                Vector<String> userIDList = new Vector<>();
                Vector<Integer> userScoreList = new Vector<>();

                System.out.print(roomName + "으로 ");
                // roomName으로 value인 Map의 key 값을 받아 userIDList에 저장

                Map<String, Integer> map = rooms.get(roomName);
                for (String userID : map.keySet()) {
                    userIDList.add(userID);
                    userScoreList.add(map.get(userID));
                    System.out.println("userID: " + userID + ", Score: " + map.get(userID) + " 전송");
                }

                SketchingData data = new SketchingData(SketchingData.MODE_CLIENT_LIST, roomName, userIDList, userScoreList);
                System.out.println("클라이언트로 데이터 전송, 방 이름: " + roomName);
                broadcast(data);
            }
        }

        private String currentPlayers() {
            String players = " <-";
            if (clients.isEmpty()) {
                return "  <접속중인 플레이어가 없습니다>";
            } else {
                for (ClientHandler client : clients) {
                    players += client.userID + "-";
                }
                players += ">";
            }
            return players;
        }

        private void broadcast(SketchingData data) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.sendData(data);
                }
            }
        }

        private void sendData(SketchingData data) {
            try {
                out.writeObject(data);
                out.flush();
            } catch (IOException e) {
                //System.err.println("클라이언트 일반 전송 오류> " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private void disconnect() {

        try {
            acceptThread = null;
            serverSocket.close();
            b_connect.setEnabled(true);
            b_exit.setEnabled(true);
            b_disconnect.setEnabled(false);

        } catch (IOException e) {
            System.err.println("서버소켓 닫기 오류> " + e.getMessage());
            System.exit(-1);
        }
    }


    private void printDisplay(String msg) {
        t_display.append(msg + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    public static void main(String[] args) {
        DrawingServer drawingServer = new DrawingServer();
        //drawingServer.startServer();
    }
}