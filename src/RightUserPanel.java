import javax.swing.*;
import java.awt.*;

public class RightUserPanel extends JPanel {
    private User user5;
    private User user6;
    private User user7;
    private User user8;

    public RightUserPanel() {
        setLayout(new GridLayout(4,0));
        user5 = new User();
        user6 = new User();
        user7 = new User();
        user8 = new User();
        add(user5);
        add(user6);
        add(user7);
        add(user8);
    }
}
