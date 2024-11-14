import javax.swing.*;
import java.awt.*;

public class LeftUserPanel extends JPanel {
    private User user1;
    private User user2;
    private User user3;
    private User user4;

    public LeftUserPanel() {
        setLayout(new GridLayout(4, 0));
        user1 = new User();
        user2 = new User();
        user3 = new User();
        user4 = new User();
        add(user1);
        add(user2);
        add(user3);
        add(user4);
    }
}
