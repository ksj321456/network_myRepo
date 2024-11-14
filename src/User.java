import javax.swing.*;
import java.awt.*;

public class User extends JPanel {
    private JTextField userId;
    private JTextField score;

    public User() {
        setLayout(new GridLayout(2, 0));
        userId = new JTextField("ID", 10);
        score = new JTextField("0", 2);
        add(userId);
        add(score);
        userId.setEnabled(false);
        score.setEnabled(false);
    }
}
