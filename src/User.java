import javax.swing.*;
import java.awt.*;

public class User extends JPanel {
    private String userId;
    private JLabel userLabel;
    private JLabel scoreLabel;

    public User() {
        setLayout(new GridLayout(2, 0));
        setLayout(new GridLayout(2, 0));
        userLabel = new JLabel();
        scoreLabel = new JLabel();
        add(userLabel);
        add(scoreLabel);
    }

    public void setUser(String userId, int score) {
        this.userId = userId;
        userLabel.setText("ID: " + userId);
        scoreLabel.setText("점수: " + score);
    }

    public String getUserId() {
        return userId;
    }

}
