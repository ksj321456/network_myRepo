import java.io.Serializable;

public class SketchingData implements Serializable {
    public static final int LINE = 1;
    public static final int CHAT = 2;

    private Line line;
    private int mode;
    private String message; // 채팅 메시지

    // 스케치데이터 전송시 생성자
    public SketchingData(int mode, Line line) {
        this.mode = mode;
        this.line = line;
        message = null;
    }

    // 채팅 메시지 전송시 생성자
    public SketchingData(int mode, String message) {
        this.mode = mode;
        this.message = message;
        line = null;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}