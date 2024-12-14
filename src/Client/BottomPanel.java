package Client;

import Buttons.LogoutBtn;
import Buttons.ScreenShotBtn;

import javax.swing.*;
import java.awt.*;

public class BottomPanel extends JPanel {
    private DrawingClient drawingClient;
    private JButton screenShotBtn;
    private JButton logoutBtn;

    public BottomPanel(DrawingClient drawingClient) {
        this.drawingClient = drawingClient;
        screenShotBtn = new ScreenShotBtn().createButton();
        logoutBtn = new LogoutBtn(drawingClient).createButton();

        setLayout(new FlowLayout(FlowLayout.CENTER));

        add(screenShotBtn); // 게임 스크린샷 버튼 추가
        add(logoutBtn); // 로그아웃 버튼 추가
    }

}