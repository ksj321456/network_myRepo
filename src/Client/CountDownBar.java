package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CountDownBar extends JPanel {
    private int timeLeft; // 남은 시간 (초)
    private int duration; // 총 시간 (초)
    private Timer timer; // 타이머 객체
    private int barWidth; // 바의 너비
    private int barHeight; // 바의 높이
    private Image pointerImage; // 헤더 포인터 이미지

    private boolean isWarning = false; // 경고 상태 flag
    private Timer warningTimer; // 시간임박 경고 타이머
    private Color warningColor = Color.RED; // 경고 깜빡임 색상

    public CountDownBar(int duration) {
        this.timeLeft = duration;
        this.duration = duration;
        this.barWidth = 350;
        this.barHeight = 50;

        ImageIcon pointerIcon = new ImageIcon("images/boogi.png");
        pointerImage = pointerIcon.getImage();
        pointerImage = pointerImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH);

        setPreferredSize(new Dimension(barWidth, barHeight));
        setBackground(Color.WHITE);

        warningTimer = new Timer(250, new ActionListener() { // 0.25초 간격으로 경고창 깜빡임
            @Override
            public void actionPerformed(ActionEvent e) {
                isWarning = !isWarning;
                // 깜빡일 때마다 색상 변경
                //warningColor = isWarning ? Color.RED : Color.WHITE;
                repaint();
            }
        });

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                if (timeLeft <= 10 && !isWarning) { // 10초 이하일 때 경고 시작
                    startWarning();
                }
                repaint();
                if (timeLeft <= 0) {
                    timer.stop();
                    stopWarning(); // 시간 종료 시 경고 멈춤
                }
            }
        });
    }

    public void start() {
        timeLeft = duration + 1; // 새로운 라운드 시작 시 다시 설정된 시간으로 초기화해서 카운트다운바 처음부터시작.
        stopWarning(); // isWarning과 warningTimer를 초기화하지 않으면, 이전 라운드에서 경고 상태가 활성화된 상태로 다음 라운드가 시작되는 문제발생.
        timer.start();
    }

    // 카운트다운 멈춤
    public void stop() {
        timer.stop();
        stopWarning();
    }

    private void startWarning() {
        isWarning = true;
        warningTimer.start();
        //playWarningSound(); // 경고음 재생
    }

    private void stopWarning() {
        isWarning = false;
        warningTimer.stop();
    }

//    private void playWarningSound() {
//        try {
//            // 경고음 파일 경로 설정
//            File soundFile = new File("sounds/warning.wav");
//            AudioClip sound = Applet.newAudioClip(soundFile.toURL());
//            sound.play();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 카운트다운 바 디폴트 색상
        g.setColor(Color.CYAN);
        int currentWidth = (int) ((double) timeLeft / duration * barWidth);
        g.fillRect(0, 0, currentWidth, barHeight);

        // 카운트다운 진행상황 색상
        g.setColor(Color.YELLOW);
        g.fillRect(currentWidth, 0, barWidth - currentWidth, barHeight);

        if (isWarning) { // 종료 임박 상태일 때
            g.setColor(warningColor);
            int borderThickness = 2; // 테두리 두께 설정 3->2
            for (int i = 0; i < borderThickness; i++) {
                g.drawRect(i, i, barWidth - 1 - 2 * i, barHeight - 1 - 2 * i);
            }
        }

        // 붓 이미지 그리기 (헤더 포인터)
        int pointerX = currentWidth;
        int pointerY = (barHeight - pointerImage.getHeight(null)) / 2;
        g.drawImage(pointerImage, pointerX, pointerY, this);
    }
}