import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// 그림판 클래스
public class DrawPanel extends JPanel {
    private List<Line> lines = new ArrayList<>();  // 선을 저장할 리스트

    public DrawPanel() {
        setBackground(Color.WHITE);
    }

    // 점 대신 선을 추가
    public void addLine(int x1, int y1, int x2, int y2) {
        lines.add(new Line(x1, y1, x2, y2));
        repaint();  // 선이 추가될 때마다 repaint 호출
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);

        // 저장된 선들을 그리기
        for (Line line : lines) {
            g.drawLine(line.x1, line.y1, line.x2, line.y2);
        }
    }
}