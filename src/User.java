import javax.swing.*;
import java.awt.*;

public class User extends JPanel {
    private String userId = "";
    private int score = 0;
    private JLabel userLabel;
    private JLabel scoreLabel;

    public User() {
        setLayout(new GridLayout(0, 2));
        userLabel = new JLabel("  ID: " + userId);
        scoreLabel = new JLabel("점수: " + score);
        add(userLabel);
        add(scoreLabel);
        setPreferredSize(new Dimension(200, 50)); // 패널의 고정 크기 설정
        setBorder(BorderFactory.createLineBorder(Color.BLACK)); // 테두리 설정
        setBackground(new Color(223, 216, 246)); // 배경색 설정
    }

    public void setUser(String userId, int score) {
        this.userId = userId;
        this.score = score;
        userLabel.setText("  ID: " + userId);
        scoreLabel.setText("점수: " + score);
    }

    public String getUserId() {
        return userId;
    }

}
