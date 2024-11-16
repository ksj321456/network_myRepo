import javax.swing.*;
import java.awt.*;

public class ChatingListPanel extends JPanel {
    private DefaultListModel<String> chatModel; //DefaultListModel=> JList에 출력될 채팅 내용들을 저장해두는 모델. DefaultListModel를 사용하여 JList에 데이터를 추가&삭제
    private JList<String> chatList; // 채팅 내용을 보여줄 JList

    public ChatingListPanel() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("채팅창", SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        chatModel = new DefaultListModel<>();
        chatList = new JList<>(chatModel);
        JScrollPane scrollPane = new JScrollPane(chatList);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addMessage(String message) {
        chatModel.addElement(message);
    }
}