package Client;

import javax.swing.*;
import java.awt.*;

public abstract class UserPanel extends JPanel {
    //protected int userCount = 0; // 현재 패널에 표시중인 유저 수
    protected User user1;
    protected User user2;
    protected User user3;
    protected User user4;

    public UserPanel(String userId) {
        setLayout(new GridLayout(4, 0));
        user1 = new User(userId);
        user2 = new User(userId);
        user3 = new User(userId);
        user4 = new User(userId);
        add(user1);
        add(user2);
        add(user3);
        add(user4);

        setBackground(new Color(180, 222, 255));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 패널의 가장자리 여백 설정
    }

    protected User getUser(int index) {
        switch (index) {
            case 0:
                return user1;
            case 1:
                return user2;
            case 2:
                return user3;
            case 3:
                return user4;
            default:
                return null;
        }
    }

}