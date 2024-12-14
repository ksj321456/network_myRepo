package Buttons;

import Client.DrawingClient;
import Main.ClientMain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogoutBtn extends TemplateButton {

    private DrawingClient drawingClient; // Client.DrawingClient 객체를 필드로 선언

    public LogoutBtn(DrawingClient drawingClient) { // 생성자에 Client.DrawingClient 객체를 매개변수로 받음
        super("images/logout.png", new Color(180, 222, 255)); // 버튼 이름, 아이콘 경로, 배경색 설정
        this.drawingClient = drawingClient;
    }

    @Override
    protected ActionListener getActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 창 닫기 확인 다이얼로그 표시 등의 작업 수행
                int result = JOptionPane.showConfirmDialog(
                        drawingClient,
                        "로그아웃 하시겠습니까?",
                        "로그아웃",
                        JOptionPane.YES_NO_OPTION
                );

                if (result == JOptionPane.YES_OPTION) {
                    drawingClient.disconnect();
                    //drawingClient = null;
                    new ClientMain(); // 로그인 화면으로 돌아가기
                }
            }
        };
    }
}
