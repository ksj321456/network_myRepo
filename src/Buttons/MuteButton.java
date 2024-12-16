package Buttons;

import etc.BgmManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MuteButton extends TemplateButton {
    private boolean isMute = false; // 음소거 상태를 저장하는 변수
    private ImageIcon muteIcon; // 음소거 아이콘
    private ImageIcon unmuteIcon; // 음소거 해제 아이콘

    public MuteButton() {
        super("images/unmute.jpg", new Color(255, 254, 133)); // 초기 아이콘과 배경색 설정
        Image muteImage = new ImageIcon("images/mute.jpg").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        this.muteIcon = new ImageIcon(muteImage);

        Image unmuteImage = new ImageIcon("images/unmute.jpg").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        this.unmuteIcon = new ImageIcon(unmuteImage);
    }

    @Override
    protected ActionListener getActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isMute) {
                    BgmManager.setVolume(4, 0.1f); // 음소거 해제
                    button.setIcon(unmuteIcon); // 음소거 해제 아이콘으로 변경
                } else {
                    BgmManager.mute(4); // 음소거
                    button.setIcon(muteIcon); // 음소거 아이콘으로 변경
                }
                isMute = !isMute; // 음소거 상태 변경
            }
        };
    }
}