import javax.swing.*;
import java.awt.*;

public class ChatingListPanel extends JPanel {
    private DefaultListModel<String> chatModel; //DefaultListModel=> JList에 출력될 채팅 내용들을 저장-관리하는 모델. DefaultListModel를 사용하여 JList에 데이터를 추가&삭제
    private JList<String> chatList; // 채팅 내용을 보여줄 JList
    private JScrollPane scrollPane;
    private JLabel l_word;
    private Font beforeStartFont;
    private Font afterStartFont;

    public ChatingListPanel() {
        setLayout(new BorderLayout()); // BorderLayout 사용
        setBackground(new Color(180, 222, 255));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 패널의 가장자리 여백 설정

        beforeStartFont = new Font("Comic Sans MS", Font.BOLD, 40); // 게임 시작 전 안내용 폰트
        afterStartFont = new Font("맑은 고딕", Font.BOLD, 30); // 게임 시작 후 제시어 전용 폰트

        l_word = new JLabel("Let's draw~!!", SwingConstants.CENTER);
        l_word.setFont(beforeStartFont); // 게임 시작전 폰트 설정
        add(l_word, BorderLayout.NORTH);

        // 채팅 리스트 추가
        chatModel = new DefaultListModel<>();
        chatList = new JList<>(chatModel);
        chatList.setCellRenderer(new ChatListCellRenderer());// 채팅리스트의 셀을 렌더링하는 방식- 즉, 이 컴포넌트에 대한 렌더러 설정

        // 셀의 고정 크기 설정
        chatList.setFixedCellHeight(20);
        chatList.setFixedCellWidth(300);

        scrollPane = new JScrollPane(chatList);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addMessage(String message, ChatType type) {

        switch (type) { // 메시지 타입에 따라 메시지 내용을 다르게 구성
            case MY_CHAT: // 본인의 채팅
                message = "나: " + message;
                break;
            case OTHERS_CHAT: // 다른 사용자의 채팅
                break;
            case SYSTEM_MESSAGE: // 시스템 메시지
                message = "[시스템] " + message;
                break;
        }
        chatModel.addElement(message);

        // <채팅들이 채팅리스트의 크기를 넘어서면 채팅리스트의 스크롤을 맨 아래로 이동시키고자 추가해준 구문>
        //스윙은 단일 스레드 모델. 즉, 모든 UI 업데이트는 EDT 스레드에서 처리
        // SwingUtilities클래스의 invokeLater() 메소드는 Runnable 객체를 Event Dispatch Thread의 이벤트 큐에 넣어주는 역할을 함
        // 이벤트 큐에 넣어진 Runnable 객체는 순차적으로 실행되기 때문에, invokeLater() 메소드를 통해 Runnable 객체를 넣어주면, Runnable 객체의 run() 메서드에 정의된 코드가 순차적으로 실행됨
        // SwingUtilities.invokeLater를 사용하지 않는 경우: 스크롤을 맨 아래로 이동시키는 코드가 EDT가 아닌 다른 스레드에서 실행될 수 있기때문에,
        // 스크롤 이동 코드가 채팅 리스트에 새로운 메시지가 추가되기 전에 실행되어 스크롤이 맨 아래로 이동되지 않는 문제가 발생함

        // 즉, 채팅리스트에 새로운 메시지가 추가된 후 -> 스크롤을 맨 아래로 이동시키는 정삭적인-순차적인 흐름을 위해 EDT스레드에서 이 작업을 수행하도록 지시해야함.
        // 그 지시가 바로 SwingUtilities.invokeLater() 메소드. 이 메소드의 인자로 전달된 Runnable 객체의 run() 메서드가 EDT에서 실행되도록 예약
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            }
        });
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
            } else if (message.startsWith("[시스템]")) {
                component.setBackground(Color.WHITE); // 시스템 메시지의 색상 설정
            } else {
                component.setBackground(Color.LIGHT_GRAY); // 다른 사용자가 입력한 메시지의 색상 설정
            }

            return component; //렌더링된 컴포넌트(꾸며진 채팅메시지 셀)를 반환
        }
    }

    public void setWord(String frondWord, String word) {
        // 제시어 부분은 파란색으로 표시하여 구분, html 태그 사용
        String fullText = "<html>" + frondWord + "<span style='color: blue;'>" + word + "</span></html>";
        l_word.setText(fullText);
    }

    public void setAfterStartFont() { // 게임 시작 후 제시어전용 폰트로 변경
        l_word.setFont(afterStartFont);
    }
}