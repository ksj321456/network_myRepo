package etc;

import java.awt.*;
import java.io.Serializable;

// 선의 속성 지정, 추후에 default 접근지정자가 아닌 private으로 변경 후 getter, setter 추가
//
public class Line implements Serializable {
    private int x1, y1, x2, y2;

    // 선의 색깔 지정, 아직 구현 X
    private Color color;

    // 선의 굵기 지정, 아직 구현 X
    private float lineWidth;


    public Line(int x1, int y1, int x2, int y2, Color color, float lineWidth) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.lineWidth = lineWidth;
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

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public Color getColor() {
        return color;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }
}