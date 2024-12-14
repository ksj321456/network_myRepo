package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InputPanel extends JPanel {
    private JTextField t_input;
    private JButton b_send;
    private DrawingClient drawingClient;

    public InputPanel(DrawingClient drawingClient) {
        this.drawingClient = drawingClient;
        setLayout(new BorderLayout());
        t_input = new JTextField(20);
        b_send = new JButton("채팅 전송");

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(); // 텍스트필드에 엔터 입력시, sendMessage 호출하여 텍스트필드에 입력한 문자열을 서버측 소켓으로 전송
            }
        };
        t_input.addActionListener(actionListener);
        b_send.addActionListener(actionListener);

        add(t_input, BorderLayout.CENTER);
        add(b_send, BorderLayout.EAST);
    }

    private void sendMessage() {
        String message = t_input.getText();
        if (!message.isEmpty()) {
            drawingClient.sendMessage(message);
            t_input.setText("");
        }
    }

    public JTextField getT_input() {
        return t_input;
    }

    public JButton getB_send() {
        return b_send;
    }
}