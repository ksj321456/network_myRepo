package Client;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class User extends JPanel {
    private String userId = "";
    private String myId = "";
    private int score = 0;
    private JLabel userLabel;
    private JLabel scoreLabel;

    public User(String myId) {
        this.myId = myId;
        setLayout(new GridLayout(2, 0));
//        userLabel = new JLabel("  ID: " + userId);
//        scoreLabel = new JLabel("점수: " + score);
        userLabel = new JLabel();
        scoreLabel = new JLabel();
        userLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(userLabel);
        add(scoreLabel);
        setPreferredSize(new Dimension(200, 50)); // 패널의 고정 크기 설정
        setBorder(BorderFactory.createLineBorder(Color.BLACK)); // 테두리 설정
        setBackground(new Color(223, 216, 246)); // 배경색 설정
    }

    public void setUser(String userId, int score) {
        this.userId = userId;
        this.score = score;

        if (userId.isEmpty()) { // 접속한 플레이어만 ID와 점수를 표시하도록 하기위함
            userLabel.setText("");
            scoreLabel.setText("");
        } else {
            userLabel.setText("ID: " + userId);
            scoreLabel.setText("점수: " + score);
        }

        if (Objects.equals(userId, myId)) {
            setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 3)); // 테두리 설정
        } else {
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
        //repaint(); // 테두리 변경 후 repaint() 호출
    }

    public String getUserId() {
        return userId;
    }

}
