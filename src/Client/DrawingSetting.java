package Client;

import Buttons.LogoutBtn;

import javax.swing.*;
import java.awt.*;

public class DrawingSetting extends JPanel {
    private DrawingClient drawingClient; // 로그아웃 버튼으로 -> 서버와의 연결을 끊기위해 Client.DrawingClient 객체 저장

    private final String[] lineColor = {"검정", "빨강", "주황", "노랑", "초록", "파랑", "보라"};
    private final String[] lineStroke = {"1px", "5px", "10px"};
    private JComboBox<String> colorBox;
    private JComboBox<String> strokeBox;
    private JButton eraser;
    private JLabel isEraser;
    private LogoutBtn logoutBtn;
    private JButton readyButton;
    private CountDownBar countDownBar;

    public DrawingSetting(CountDownBar countDownBar) {
        this.countDownBar = countDownBar;
        setLayout(new BorderLayout());
        JPanel settingsPanel = new JPanel(new FlowLayout());
        settingsPanel.setBackground(new Color(195, 234, 253));
        colorBox = new JComboBox<>(lineColor);
        strokeBox = new JComboBox<>(lineStroke);
        eraser = new JButton("지우개");
        isEraser = new JLabel("지우개 사용중 X");
        readyButton = new JButton("준비버튼");

        settingsPanel.add(readyButton);
        settingsPanel.add(colorBox);
        settingsPanel.add(strokeBox);
        settingsPanel.add(eraser);
        settingsPanel.add(isEraser);
        add(settingsPanel, BorderLayout.CENTER);


        //countDownBar.setPreferredSize(new Dimension(300, 50)); // 예시: 너비 100, 높이 70으로 고정
        // 왼쪽에 CountDownBar를 배치할 패널 생성
        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 왼쪽 정렬
        westPanel.add(countDownBar);
        add(westPanel, BorderLayout.WEST); // BorderLayout의 WEST 영역에 추가
        //add(logoutBtn, BorderLayout.EAST);

    }

    public Color getSelectedColor() {
        String selectedColor = (String) colorBox.getSelectedItem();
        switch (selectedColor) {
            case "검정":
                return Color.BLACK;
            case "빨강":
                return Color.RED;
            case "주황":
                return Color.ORANGE;
            case "노랑":
                return Color.YELLOW;
            case "초록":
                return Color.GREEN;
            case "파랑":
                return Color.BLUE;
            case "보라":
                return Color.MAGENTA;
            default:
                return Color.BLACK; // 기본 색상
        }
    }

    public float getSelectedLineWidth() {
        String selectedWidth = (String) strokeBox.getSelectedItem();
        switch (selectedWidth) {
            case "1px":
                return 1;
            case "5px":
                return 5;
            case "10px":
                return 10;
            // 기본 굵기 1
            default:
                return 1;
        }
    }

    public JButton getReadyButton() {
        return readyButton;
    }

    public String[] getLineColor() {
        return lineColor;
    }

    public String[] getLineStroke() {
        return lineStroke;
    }

    public JComboBox<String> getColorBox() {
        return colorBox;
    }

    public void setColorBox(JComboBox<String> colorBox) {
        this.colorBox = colorBox;
    }

    public JComboBox<String> getStrokeBox() {
        return strokeBox;
    }

    public void setStrokeBox(JComboBox<String> strokeBox) {
        this.strokeBox = strokeBox;
    }

    public JButton getEraser() {
        return eraser;
    }

    public void setEraser(JButton eraser) {
        this.eraser = eraser;
    }

    public JLabel getIsEraser() {
        return isEraser;
    }

    public void setIsEraser(JLabel isEraser) {
        this.isEraser = isEraser;
    }
}
