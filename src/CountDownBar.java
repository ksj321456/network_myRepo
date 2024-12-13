import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CountDownBar extends JPanel {
    private int timeLeft; // 남은 시간 (초)
    private Timer timer; // 타이머 객체
    private int barWidth; // 바의 너비
    private int barHeight; // 바의 높이
    private Image pointerImage; // 헤더 포인터 이미지

    public CountDownBar(int duration) {
        this.timeLeft = duration;

        this.barWidth = 350;
        this.barHeight = 50;

        ImageIcon pointerIcon = new ImageIcon("images/boogi.png"); // 붓 이미지 경로 설정
        pointerImage = pointerIcon.getImage();
        pointerImage = pointerImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH); // 이미지 크기 조절


        setPreferredSize(new Dimension(barWidth, barHeight));
        setBackground(Color.WHITE);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                repaint();
                if (timeLeft <= 0) {
                    timer.stop();
                }
            }
        });
    }

    public void start() {
        timeLeft = 60; // 라운드 시작 시 시간 초기화
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 파란색 바 그리기
        g.setColor(Color.CYAN); // 파란색 설정
        int currentWidth = (int) ((double) timeLeft / 60 * barWidth);
        g.fillRect(0, 0, currentWidth, barHeight);

        // 노란색 바 그리기
        g.setColor(Color.YELLOW); // 노란색 설정
        g.fillRect(currentWidth, 0, barWidth - currentWidth, barHeight);

        // 붓 이미지 그리기 (헤더 포인터)
        int pointerX = currentWidth;
        int pointerY = (barHeight - pointerImage.getHeight(null)) / 2;
        g.drawImage(pointerImage, pointerX, pointerY, this);
    }
}