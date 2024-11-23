import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClientMain extends JFrame {

    private JTextField t_UserName;
    private JTextField t_IpAddress;
    private JTextField t_PortNumber;
    private JPanel mainPanel = new JPanel();

    public ClientMain() {
        setTitle("Hansung Sketch");
        setResizable(false);
        /* 프레임의 위치를 화면 중앙으로 설정하는 절차들 */
        // 현재 사용자의 모니터 화면의 크기를 가져옴
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // 화면의 가로세로 중앙 계산
        int centerX = (int) (screenSize.getWidth() - 550) / 2;
        int centerY = (int) (screenSize.getHeight() - 600) / 2;
        setBounds(centerX, centerY, 550, 600);
        /*-------------------------------------*/
        buildGUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void buildGUI() {


        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel l_Title = new JLabel("Hansung Sketch");
        l_Title.setFont(new Font("Comic Sans MS", Font.BOLD, 40));

        // JLabel의 크기를 텍스트에 맞게 조절
        l_Title.setSize(l_Title.getPreferredSize());
        l_Title.setLocation(95, 70);


        mainPanel.add(l_Title);

        JLabel l_UserName = new JLabel("Player Name");
        l_UserName.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));

        l_UserName.setBounds(138, 209, 102, 33);
        mainPanel.add(l_UserName);

        t_UserName = new JTextField();
        t_UserName.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        t_UserName.setHorizontalAlignment(SwingConstants.CENTER);
        t_UserName.setBounds(269, 209, 116, 33);
        mainPanel.add(t_UserName);
        t_UserName.setColumns(10);

        JLabel lblIpAddress = new JLabel("IP Address");
        lblIpAddress.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
        lblIpAddress.setBounds(138, 283, 102, 33);
        mainPanel.add(lblIpAddress);

        t_IpAddress = new JTextField();
        t_IpAddress.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        t_IpAddress.setHorizontalAlignment(SwingConstants.CENTER);
        t_IpAddress.setText("127.0.0.1");
        t_IpAddress.setColumns(10);
        t_IpAddress.setBounds(269, 283, 116, 33);
        mainPanel.add(t_IpAddress);

        JLabel lblPortNumber = new JLabel("Port Number");
        lblPortNumber.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
        lblPortNumber.setBounds(138, 362, 102, 33);
        mainPanel.add(lblPortNumber);

        t_PortNumber = new JTextField();
        t_PortNumber.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        t_PortNumber.setText("12345");
        t_PortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        t_PortNumber.setColumns(10);
        t_PortNumber.setBounds(269, 362, 116, 33);
        mainPanel.add(t_PortNumber);

        JButton btnConnect = new JButton("Game Start");

// 폰트 변경
        btnConnect.setFont(new Font("Comic Sans MS", Font.BOLD, 20));

// 배경색 변경
        btnConnect.setBackground(new Color(255, 255, 153)); // 연한 노란색

//// 둥근 모서리 설정
//        btnConnect.setBorder(BorderFactory.createRoundedBorder(new BorderRadius(20)));

// 그림자 효과 추가
        btnConnect.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.lightGray, 3), // 회색 외곽선
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // 내부 여백
        ));


        btnConnect.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnConnect.setBackground(new Color(241, 167, 7));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnConnect.setBackground(new Color(255, 255, 153)); //원래 색상으로
            }
        });

        btnConnect.setBounds(181, 445, 162, 48);

        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userName = t_UserName.getText().trim();
                String ipAddress = t_IpAddress.getText().trim();
                String portNumber = t_PortNumber.getText().trim();
                startGame(userName, ipAddress, Integer.parseInt(portNumber));
            }
        });
        mainPanel.add(btnConnect);

        mainPanel.setLayout(null);
        setContentPane(mainPanel);
    }

    private void startGame(String userName, String ipAddress, int portNumber) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    DrawingClient drawingClient = new DrawingClient(userName, ipAddress, portNumber);
                    drawingClient.setVisible(true);
                    dispose(); // 현재 창 닫기
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ClientMain frame = new ClientMain();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}