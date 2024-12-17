package Main;

import Lobby.LobbyClient;
import etc.BgmManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class ClientMain extends JFrame {
    InetAddress inetAddress = null;
    private JTextField t_UserName;
    private JTextField t_IpAddress;
    private JTextField t_PortNumber;
    private JPanel mainPanel = new JPanel();

    public ClientMain() {
        setTitle("Hansung Sketch");

        /* //프레임의 위치를 화면 중앙으로 설정하는 절차들
        // 현재 사용자의 모니터 화면의 크기를 가져옴
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // 화면의 가로세로 중앙 계산
        int centerX = (int) (screenSize.getWidth() - 550) / 2;
        int centerY = (int) (screenSize.getHeight() - 600) / 2;
        setBounds(centerX, centerY, 520, 500);*/
        setSize(520, 500);
        setLocationRelativeTo(null); // 화면 중앙에 프레임 띄우기
        buildGUI();
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BgmManager.loopAudio(2); // 메인화면 bgm 무한재생
    }

    private void buildGUI() {

        mainPanel.setBackground(new Color(180, 222, 255));
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(null); // 절대 위치 레이아웃 사용

        JLabel l_Title = new JLabel("Hansung Sketch");
        l_Title.setFont(new Font("Comic Sans MS", Font.BOLD, 40));

        // JLabel의 크기를 텍스트에 맞게 조절
        l_Title.setSize(400, 60);
        //l_Title.setSize(l_Title.getPreferredSize());
        l_Title.setLocation(85, 30); // 제목 위치 조정


        mainPanel.add(l_Title);

        // pencil 아이콘 추가
        ImageIcon pencilIcon = new ImageIcon("images/pencil.png");
        Image pencilImage = pencilIcon.getImage(); // ImageIcon을 Image로 변환
        Image scaledPencilImage = pencilImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH); // 이미지 크기 조절
        ImageIcon scaledPencilIcon = new ImageIcon(scaledPencilImage); // 다시 ImageIcon으로 변환
        JLabel l_pencil = new JLabel(scaledPencilIcon);
        l_pencil.setBounds(25, 35, 50, 50); // 아이콘 위치 및 크기 조정
        mainPanel.add(l_pencil);

        // palette 아이콘 추가
        ImageIcon paletteIcon = new ImageIcon("images/palette.png");
        Image paletteImage = paletteIcon.getImage(); // ImageIcon을 Image로 변환
        Image scaledPaletteImage = paletteImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH); // 이미지 크기 조절
        ImageIcon scaledPaletteIcon = new ImageIcon(scaledPaletteImage); // 다시 ImageIcon으로 변환
        JLabel l_palette = new JLabel(scaledPaletteIcon);
        l_palette.setBounds(410, 35, 50, 50); // 아이콘 위치 및 크기 조정
        mainPanel.add(l_palette);

/*        // 물결 밑줄 추가
        JLabel l_Underline = new JLabel("~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        l_Underline.setFont(new Font("MV Boli", Font.PLAIN, 20)); // 폰트 변경
        l_Underline.setBounds(70, 80, 350, 30); // 밑줄 위치 및 크기 조정
        mainPanel.add(l_Underline);*/

        JLabel l_UserName = new JLabel("Player Name");
        l_UserName.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));

        l_UserName.setBounds(123, 139, 102, 33);
        mainPanel.add(l_UserName);

        t_UserName = new JTextField();
        t_UserName.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        t_UserName.setHorizontalAlignment(SwingConstants.CENTER);
        t_UserName.setBounds(254, 139, 116, 33);
        mainPanel.add(t_UserName);
        t_UserName.setColumns(10);

        JLabel lblIpAddress = new JLabel("IP Address");
        lblIpAddress.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
        lblIpAddress.setBounds(123, 213, 102, 33);
        mainPanel.add(lblIpAddress);

        t_IpAddress = new JTextField();
        t_IpAddress.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        t_IpAddress.setHorizontalAlignment(SwingConstants.CENTER);
        t_IpAddress.setText("127.0.0.1");
        t_IpAddress.setColumns(10);
        t_IpAddress.setBounds(254, 213, 116, 33);
        mainPanel.add(t_IpAddress);

        try {
            inetAddress = InetAddress.getLocalHost();
            t_IpAddress.setText(inetAddress.getHostAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        JLabel lblPortNumber = new JLabel("Port Number");
        lblPortNumber.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
        lblPortNumber.setBounds(123, 292, 102, 33);
        mainPanel.add(lblPortNumber);

        t_PortNumber = new JTextField();
        t_PortNumber.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        t_PortNumber.setText("12345");
        t_PortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        t_PortNumber.setColumns(10);
        t_PortNumber.setBounds(254, 292, 116, 33);
        t_PortNumber.setEditable(false); // 포트번호 수정 불가
        mainPanel.add(t_PortNumber);

        JButton startButton = new JButton("Let's Start!");


        startButton.setFont(new Font("Comic Sans MS", Font.BOLD, 20));


        startButton.setBackground(new Color(255, 255, 153)); // 연한 노란색


        startButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.lightGray, 3), // 회색 외곽선
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // 내부 여백
        ));


        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                startButton.setBackground(new Color(241, 167, 7));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                startButton.setBackground(new Color(255, 255, 153)); //원래 색상으로
            }
        });

        startButton.setBounds(166, 375, 162, 48);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = t_UserName.getText().trim();
                String ipAddress = t_IpAddress.getText().trim();
                String portNumber = t_PortNumber.getText().trim();
                startGame(userName, ipAddress, Integer.parseInt(portNumber));
            }
        });
        mainPanel.add(startButton);

        setContentPane(mainPanel);
    }

    private void startGame(String userName, String ipAddress, int portNumber) {
        BgmManager.stopAudio(2); // BGM 중지
        dispose(); // 현재 창 닫기
        LobbyClient lobbyClient = new LobbyClient(userName, ipAddress, portNumber);

    }


    public static void main(String[] args) {
        ClientMain frame = new ClientMain();
    }
}