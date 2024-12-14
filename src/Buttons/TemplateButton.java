package Buttons;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionListener;

//Template Method Pattern을 적용하여 커스텀 버튼 재사용성 높이기
public abstract class TemplateButton {
    protected JButton button;
    protected ImageIcon icon; // 버튼 이미지
    protected Color backgroundColor; // 버튼 배경 색상

    public TemplateButton(String imagePath, Color backgroundColor) { // 생성자를 통해 버튼의 속성을 초기화
        this.icon = new ImageIcon(imagePath);
        this.backgroundColor = backgroundColor;
    }

    public JButton createButton() {
        icon = new ImageIcon(icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)); // 이미지 크기 조절

        button = new JButton(icon);
        button.setBackground(backgroundColor);
        button.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null)); // 테두리 에칭 효과. 버튼이 입체적으로 보이도록.
        button.addActionListener(getActionListener()); // ActionListener 설정은 하위 클래스에서 구현

        return button;
    }

    protected abstract ActionListener getActionListener(); // 구체 클래스들이 각각 구현한 ActionListener를 반환하는 추상 메서드
}
