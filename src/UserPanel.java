import javax.swing.*;
import java.awt.*;

public abstract class UserPanel extends JPanel {
    protected int userCount = 0; // 현재 패널에 표시중인 유저 수
    protected User user1;
    protected User user2;
    protected User user3;
    protected User user4;

    public UserPanel() {
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

    public boolean addUser(String userId, int score) {
        if (userCount < 4) {
            User user = getUserByIndex(userCount);
            if (user != null) {
                user.setUser(userId, score);
                userCount++;
                revalidate();
                repaint();
                return true;
            }
        }
        return false;
    }

    public boolean removeUser(String userId) {
        for (int i = 0; i < 4; i++) {
            User user = getUserByIndex(i);
            if (user != null && userId.equals(user.getUserId())) {
                user.setUser("", 0);
                userCount--;
                revalidate();
                repaint();
                return true;
            }
        }
        return false;
    }

    protected User getUserByIndex(int index) {
        switch (index) {
            case 0: return user1;
            case 1: return user2;
            case 2: return user3;
            case 3: return user4;
            default: return null;
        }
    }

    public int getUserCount() {
        return userCount;
    }
}