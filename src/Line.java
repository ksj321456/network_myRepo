import java.awt.*;
import java.io.Serializable;

// 선의 속성 지정, 추후에 default 접근지정자가 아닌 private으로 변경 후 getter, setter 추가
//
public class Line implements Serializable {
    private int x1, y1, x2, y2;

    // 선의 색깔 지정, 아직 구현 X
    private Color color;

    // 선의 굵기 지정, 아직 구현 X
    private double lineWidth;

    public Line(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public Color getColor() {
        return color;
    }

    public double getLineWidth() {
        return lineWidth;
    }
}