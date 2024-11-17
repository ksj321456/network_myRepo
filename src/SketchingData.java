import java.io.Serializable;

public class SketchingData implements Serializable {
    public static final int LINE = 1;

    private Line line;
    private int mode;

    // 그림 그리기 서버에 전송 시 생성자
    public SketchingData(int mode, Line line) {
        this.mode = mode;
        this.line = line;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
