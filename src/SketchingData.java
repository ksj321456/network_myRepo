import java.io.Serializable;
import java.util.Vector;

public class SketchingData implements Serializable {


    public static final int MODE_LOGIN = 1;
    public static final int MODE_LOGOUT = 2;
    public static final int MODE_CHAT = 3;
    public static final int MODE_LINE = 4;
    public static final int MODE_CLIENT_LIST = 5; // 접속자 리스트를 브로드케스팅하기 위한 모드
/*
    public static final int MODE_CORRECT = 6; // 플레이어가 제시어를 맞췄음을 알리기 위한 모드
    public static final int MODE_READY = 7; // 게임 시작 준비가 완료되었음을 알리는 모드
    public static final int GAME_START = 8; // 모든 사용자들이 게임 시작를 수락 ( + 2명 이상 접속해있는경우) -> 게임이 시작되었음을 알리는 모드(현재 접속자 인원이랑 게임 시작 준비 인원이랑 같을 때)
    public static final int GAME_OVER = 9; // 정해진 모든 라운드가 종료되어(or 한 플레이어가 특정 maximum 점수에 도달하면) 게임이 종료되었음을 알리는 모드
*/

    private Line line;
    private int mode;
    private String message; // 채팅 메시지
    private String userID; // 접속한 사용자 ID
    private Vector<String> userIDList; // 접속한 사용자 ID 리스트
    private Vector<Integer> userScoreList; // 접속한 사용자들의 점수 리스트

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

    // 플레이어&점수리스트 전송용 생성자
    public SketchingData(int mode, Vector<String> userIDList, Vector<Integer> userScoreList) {
        this.mode = mode;
        this.userIDList = userIDList;
        this.userScoreList = userScoreList;
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

    public Vector<String> getuserIDList() {
        return userIDList;
    }

    public Vector<Integer> getuserScoreList() {
        return userScoreList;
    }
}