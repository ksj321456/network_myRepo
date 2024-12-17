package Client;

import Buttons.LogoutBtn;
import Buttons.MuteButton;
import Buttons.ScreenShotBtn;

import javax.swing.*;
import java.awt.*;

public class BottomPanel extends JPanel {
    private DrawingClient drawingClient;
    private JButton screenShotBtn;
    private JButton logoutBtn;
    private JButton muteBtn;
    private JLabel coordinates;

    public BottomPanel(DrawingClient drawingClient) {
        this.drawingClient = drawingClient;
        screenShotBtn = new ScreenShotBtn().createButton();
        logoutBtn = new LogoutBtn(drawingClient).createButton();
        muteBtn = new MuteButton().createButton();
        coordinates = new JLabel("좌표값: ");
        coordinates.setHorizontalAlignment(SwingConstants.CENTER); // 가운데 정렬

        setLayout(new GridLayout(1, 4)); // 1행 4열
        setBackground(new Color(210, 235, 248));
        add(coordinates);
        add(screenShotBtn); // 게임 스크린샷 버튼 추가
        add(muteBtn); // 음소거 버튼 추가
        add(logoutBtn); // 로그아웃 버튼 추가

    }

    public JLabel getCoordinates() {
        return coordinates;
    }
}