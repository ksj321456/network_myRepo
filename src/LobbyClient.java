import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

public class LobbyClient extends JFrame {
    private JButton btn_createRoom, btn_joinRoom;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private Socket socket = null;
    private ObjectOutputStream out = null;
    private String userName, ipAddress;
    private int portNumber;
    private Thread receiveThread = null;
    ObjectInputStream in = null;

    public LobbyClient(String userName, String ipAddress, int portNumber) {
        this.userName = userName;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        setTitle("Hansung Sketch Client");
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 서버와 통신할 소켓과 스트림 생성
        connectToServer();
        buildLobbyUI();
        sendUserID();
        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    private void connectToServer() {
        try {
            socket = new Socket(ipAddress, portNumber);
            out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ReceiveThread extends Thread {


        public ReceiveThread() {
            try {
                in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            while (receiveThread == Thread.currentThread()) {
                try {
                    SketchingData data = (SketchingData) in.readObject();
                    if (data.getMode() == SketchingData.CREATE_ROOM) {
                        // 서버에서의 broadcast를 하나의 클라이언트에서만 받을 수 있도록 조건문 추가
                        if (userName.equals(data.getOwnerName())) {
                            // 서버로부터 방 생성 정보 수신
                            String roomName = data.getRoomName();

                            // 방 목록에 새로운 방 추가
                            roomListModel.addElement(roomName);
                            //how to stop the thread ?


                            dispose();
                            receiveThread = null;

                            new DrawingClient(socket, out, in, data.getRoomName(), data.getOwnerName(), data.getIPAddress(), data.getPortNumber());
                            System.out.println("새로운 방 추가됨: " + roomName);

                        }
                    } else if (data.getMode() == SketchingData.SHOW_ROOM_LIST) {
                        if (userName.equals(data.getUserID())) {
                            Vector<String> roomList = data.getRoomList();
                            roomListModel.clear(); // 기존 목록을 초기화
                            for (String room : roomList) {
                                roomListModel.addElement(room); // 방 목록 추가
                            }
                            System.out.println("방 목록 업데이트 완료");
                        }
                    }
                    // 방에 입장
                    else if (data.getMode() == SketchingData.ENTER_ROOM) {
                        if (userName.equals(data.getOwnerName())) {
                            dispose();
                            receiveThread = null;
                            new DrawingClient(socket, out, in, data.getRoomName(), data.getOwnerName(), data.getIPAddress(), data.getPortNumber());
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void sendUserID() {
        send(new SketchingData(SketchingData.MODE_LOGIN, userName)); // 서버에게 로그인 모드&사용자 아이디값을 전달. 서버가 이 아이디값을 통해 클라이언트를 식별.
    }

    private void send(SketchingData data) {
        try {
            out.writeObject(data);
            out.flush();
        } catch (IOException e) {
            System.err.println("클라이언트 일반 전송 오류> " + e.getMessage());
            System.exit(0);
        }
    }

    private void buildLobbyUI() {
        JPanel lobbyPanel = new JPanel(new BorderLayout());

        // 방 목록 표시용 JList
        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        JScrollPane roomScroll = new JScrollPane(roomList);

        // 버튼 추가
        btn_createRoom = new JButton("방 만들기");
        btn_joinRoom = new JButton("입장하기");

        // 버튼 이벤트 핸들러
        btn_createRoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRoomCreationDialog();
            }
        });

        btn_joinRoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = roomList.getSelectedIndex(); // 선택된 방 인덱스
                if (selectedIndex != -1) {
                    String selectedRoom = roomListModel.get(selectedIndex); // 선택된 방 이름
                    System.out.println("입장 " + selectedRoom + "!");

                    // DrawingClient로 이동
                    String roomName = selectedRoom.split(" ")[0];  // 방 이름만 추출 (방장:이름 부분 제거)

                    // 서버에 입장하고자 하는 데이터 전송 => 모드값, 방 이름, 유저 이름
                    send(new SketchingData(SketchingData.ENTER_ROOM, roomName, userName, ipAddress, portNumber));
                } else {
                    System.out.println("선택된 방이 없음");
                }
            }
        });

        JPanel controlPanel = new JPanel(new GridLayout(1, 2));
        controlPanel.add(btn_createRoom);
        controlPanel.add(btn_joinRoom);

        lobbyPanel.add(roomScroll, BorderLayout.CENTER);
        lobbyPanel.add(controlPanel, BorderLayout.SOUTH);

        add(lobbyPanel);

        setVisible(true);
    }

    private void openRoomCreationDialog() {
        JDialog dialog = new JDialog(this, "Create Room", true);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);

        JLabel lbl_roomName = new JLabel("방 이름:");
        JTextField txt_roomName = new JTextField();

        JLabel lbl_maxPlayers = new JLabel("최대 인원수:");
        JTextField txt_maxPlayers = new JTextField();

        JButton btn_ok = new JButton("확인");
        JButton btn_cancel = new JButton("취소");

        btn_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = txt_roomName.getText().trim();
                String maxPlayersText = txt_maxPlayers.getText().trim();

                if (roomName.isEmpty() || maxPlayersText.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "모든 컴포넌트에 입력해야함", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int maxPlayers = Integer.parseInt(maxPlayersText);
                    if (maxPlayers < 2 || maxPlayers > 8) {
                        JOptionPane.showMessageDialog(dialog, "2 ~ 8 까지의 정수여야함.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    send(new SketchingData(SketchingData.CREATE_ROOM, roomName, userName, ipAddress, portNumber));
                    System.out.println("서버에 방 생성 정보 전송");
                    String roomInfo = roomName + " (최대 인원수 : " + maxPlayers + ")";
//                    roomListModel.addElement(roomInfo); // 방 목록에 추가
//                    System.out.println("방 생성: " + roomInfo);

                    dialog.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "최대 인원수에는 숫자만 입력 가능", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btn_cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.add(lbl_roomName);
        dialog.add(txt_roomName);
        dialog.add(lbl_maxPlayers);
        dialog.add(txt_maxPlayers);
        dialog.add(btn_ok);
        dialog.add(btn_cancel);

        dialog.setVisible(true);
    }
//
//    public static void main(String[] args) {
//
//    }
}