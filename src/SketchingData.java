import java.io.Serializable;

public class SketchingData implements Serializable {


    public static final int MODE_LOGIN = 1;
    public static final int MODE_LOGOUT = 2;
    public static final int MODE_CHAT = 3;
    public static final int MODE_LINE = 4;
    public static final int MODE_CLIENT_LIST = 5; // 접속자 리스트

    private Line line;
    private int mode;
    private String message; // 채팅 메시지
    private String userID; // 접속한 사용자 ID

    // 로그인, 로그아웃용 생성자
    public SketchingData(int mode, String userID) {
        this.mode = mode;
        this.userID = userID;
    }

    // 스케치데이터 전송시 생성자
    public SketchingData(int mode, Line line) {
        this.mode = mode;
        this.line = line;
        message = null;
    }

    // 채팅 메시지 전송시 생성자
    public SketchingData(int mode, String userID, String message) {
        this.mode = mode;
        this.userID = userID;
        this.message = message;

    }

    public String getUserID() {
        return userID;
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