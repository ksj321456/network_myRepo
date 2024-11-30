import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DrawingServer extends JFrame {
    private static final int PORT = 12345;
    private ServerSocket serverSocket = null;
    private Thread acceptThread = null;
    private Vector<ClientHandler> clients = new Vector<>();
    private JTextArea t_display;
    private JButton b_connect, b_disconnect, b_exit;

    private Vector<String> roomNamesList = new Vector<>();
    private Vector<String> ownerNamesList = new Vector<>();
    private Map<String, DrawingClient> rooms = new HashMap<>();

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
                        sendPlayerList(); // 각 클라이언트에게 현재 접속중인 플레이어 리스트 전송
                        printDisplay("NEW 플레이어: " + userID);
                        printDisplay("현재 접속중인 플레이어 수: " + clients.size() + currentPlayers());

                        // 로그인시 방이 하나라도 있다면 새로운 클라이언트는 존재하는 방들을 확인해야 한다.
                        if (!rooms.isEmpty()) {
                            // 현재 있는 방의 이름들을 roomNames Vector에 저장
                            Vector<String> roomNames = new Vector<>();
                            for (String roomName : rooms.keySet()) {
                                roomNames.add(roomName);
                            }
                            // 로그인한 클라이언트에게만 현재 방 리스트들을 전송
                            sendOnlyOne(new SketchingData(SketchingData.SHOW_ROOM_LIST, roomNames), data.getUserID());
                        }

                        continue;
                    } else if (data.getMode() == SketchingData.MODE_LOGOUT) { // 로그아웃 메시지라면
                        break; // 클라이언트측과의 연결을 해제
                    }                    //채팅 메시지를 받았을 때
                    else if (data.getMode() == SketchingData.MODE_CHAT) {
                        printDisplay("[채팅 메시지]" + userID + ": " + data.getMessage());
                        broadcast(data);
                    } else if (data.getMode() == SketchingData.MODE_LINE) {
                        Line line = data.getLine();
                        //printDisplay("그리기 좌표: " + line.getX1() + ", " + line.getY1() + ", " + line.getX2() + ", " + line.getY2());
                        broadcast(data);
                    } else if (data.getMode() == SketchingData.CREATE_ROOM) {
                        // 방 생성 요청을 받았을 때

                        // 생성하고자하는 방의 이름이 중복된게 없을 때
                        if (!rooms.containsKey(data.getRoomName())) {
                            DrawingClient drawingClient = new DrawingClient(data.getRoomName(), data.getOwnerName(), data.getIPAddress(), data.getPortNumber());
                            rooms.put(data.getRoomName(), drawingClient);
                            printDisplay("방 생성 (방 이름: " + data.getRoomName() + ") 방 갯수: " + rooms.size());
                            broadcast(new SketchingData(SketchingData.CREATE_ROOM, data.getRoomName(), data.getOwnerName(), data.getIPAddress(), data.getPortNumber()));
                        }
                    }
                    // 방에 입장할 때
                    else if (data.getMode() == SketchingData.ENTER_ROOM) {
                        // 전달받은 roomName 속성을 통해 방을 찾고 해당 DrawingClient 불러오기
                        for (String roomName : rooms.keySet()) {
                            if (roomName.equals(data.getRoomName())) {
                                new DrawingClient(data.getRoomName(), data.getOwnerName(), data.getIPAddress(), data.getPortNumber());
                            }
                        }
                    }
                }
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
            Vector<String> userIDList = new Vector<>();
            Vector<Integer> userScoreList = new Vector<>();

            for (ClientHandler client : clients) {
                userIDList.add(client.userID);
                userScoreList.add(client.score);
            }
            SketchingData data = new SketchingData(SketchingData.MODE_CLIENT_LIST, userIDList, userScoreList);
            broadcast(data);
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
            synchronized(clients) {
                for (ClientHandler client : clients) {
                    client.sendData(data);
                }
            }
        }

        // 하나의 클라이언트에게만 데이터 전송
        private void sendOnlyOne(SketchingData data, String userID) {
            synchronized (clients) {
                for(ClientHandler clientHandler : clients) {
                    if (clientHandler.userID.equals(userID)) {
                        sendData(data);
                    }
                }
            }
        }

        private void broadcastOthers(SketchingData data, ClientHandler sender) {
            for (ClientHandler client : clients) {
                if (client != sender) {  // 전송자를 제외하고 브로드캐스팅
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