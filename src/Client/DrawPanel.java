package Client;

import etc.Line;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// 그림판 클래스
public class DrawPanel extends JPanel {
    // 선을 저장할 리스트
    private List<Line> lines = new ArrayList<>();

    public DrawPanel() {
        setBackground(Color.WHITE); // 배경색 설정
    }

    // 점 대신 선을 추가 (색상 포함)
    public synchronized void addLine(int x1, int y1, int x2, int y2, Color color, float lineWidth) {
        lines.add(new Line(x1, y1, x2, y2, color, lineWidth));
        repaint(); // 선이 추가될 때마다 다시 그리기
    }

    // 현재 패널에 그려져 있는 그림 전체 지우기
    public synchronized void clear() {
        lines.clear();
        repaint();
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 저장된 선들을 그리기
        for (Line line : lines) {
            g2d.setColor(line.getColor()); // 선의 색상 설정
            g2d.setStroke(new BasicStroke(line.getLineWidth())); // 선의 굵기 설정
            g2d.drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
        }
    }
}
