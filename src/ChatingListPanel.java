import javax.swing.*;
import java.awt.*;

public class ChatingListPanel extends JPanel {
    private DefaultListModel<String> chatModel; //DefaultListModel=> JList에 출력될 채팅 내용들을 저장-관리하는 모델. DefaultListModel를 사용하여 JList에 데이터를 추가&삭제
    private JList<String> chatList; // 채팅 내용을 보여줄 JList

    public ChatingListPanel() {
        setLayout(new BorderLayout());

//        JLabel titleLabel = new JLabel("채팅창", SwingConstants.CENTER);
//        add(titleLabel, BorderLayout.NORTH);

        chatModel = new DefaultListModel<>();
        chatList = new JList<>(chatModel);
        chatList.setCellRenderer(new ChatListCellRenderer()); // 채팅리스트의 셀을 렌더링하는 방식- 즉, 이 컴포넌트에 대한 렌더러 설정

        // 셀의 고정 크기 설정
        chatList.setFixedCellHeight(30);
        chatList.setFixedCellWidth(200);

        JScrollPane scrollPane = new JScrollPane(chatList);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addMessage(String message) {
        chatModel.addElement(message);
    }

    // JList의 각 셀(채팅 메시지)의 렌더링 방식(색-모양, 이벤트 등등)을 커스텀하게해주는 ListCellRenderer 커스텀클래스
    private class ChatListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // 기본렌더링된 컴포넌트(채팅메시지 셀)를 반환
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String message = (String) value; // JList의 각 셀. 채팅 메시지

            // 각 채팅 메시지에 색을 입히는 로직
            if (message.startsWith("나:")) {
                component.setBackground(Color.CYAN); // 본인이 입력한 메시지의 색상 설정
            } else {
                component.setBackground(Color.LIGHT_GRAY); // 다른 사용자가 입력한 메시지의 색상 설정
            }

            return component; //렌더링된 컴포넌트(꾸며진 채팅메시지 셀)를 반환
        }
    }
}