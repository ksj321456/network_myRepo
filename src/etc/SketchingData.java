package etc;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.Vector;

public class SketchingData implements Serializable {


    public static final int MODE_LOGIN = 1;
    public static final int MODE_LOGOUT = 2;
    public static final int MODE_CHAT = 3;
    public static final int MODE_LINE = 4;
    public static final int MODE_CLIENT_LIST = 5; // 접속자 리스트를 브로드케스팅하기 위한 모드

    public static final int MODE_CORRECT = 6; // 플레이어가 제시어를 맞췄음을 알리기 위한 모드
    public static final int MODE_INDIVIDUAL_READY = 7;      // 한 명의 클라이언트가 준비 버튼을 눌렀을 때의 모드
    public static final int GAME_START = 8; // 모든 사용자들이 게임 시작를 수락 ( + 2명 이상 접속해있는경우) -> 게임이 시작되었음을 알리는 모드(현재 접속자 인원이랑 게임 시작 준비 인원이랑 같을 때)
    public static final int ROUND_START = 9;   // 각 게임 라운드가 시작될 때
    public static final int GAME_OVER = 10; // 정해진 모든 라운드가 종료되어(or 한 플레이어가 특정 maximum 점수에 도달하면) 게임이 종료되었음을 알리는 모드

    public static final int CREATE_ROOM = 11;   // 방을 생성하는 모드
    public static final int SHOW_ROOM_LIST = 12;        // 새로운 클라이언트에게 방의 리스트들을 보여주는 모드
    public static final int ENTER_ROOM = 13;        // 방에 입장하는 모드

    private Line line;
    private int mode;
    private String message; // 채팅 메시지
    private String userID; // 접속한 사용자 ID
    private Vector<String> userIDList; // 접속한 사용자 ID 리스트
    private Vector<Integer> userScoreList; // 접속한 사용자들의 점수 리스트
    private String roomName;
    private String ownerName;
    private String IPAddress;
    private int portNumber;
    private Vector<String> roomList;

    // 방에 접속해 있는 클라이언트들의 수
    private Vector<Integer> userCnt;

    // 준비를 하는지 취소하는지
    private boolean isReady;

    // 준비완료 요청이 성공됐는지
    private boolean isSuccess;

    private SortedMap<String, Integer> sortedMap;

    // 로그인, 로그아웃용 생성자 + 게임 시작을 알리는 프로토콜
    public SketchingData(int mode, String userID) {
        this.mode = mode;
        this.userID = userID;
    }

    // 문제를 맞췄을 경우의 프로토콜
    public SketchingData(int mode, String roomName, String userID, Vector<String> userIDList, Vector<Integer> userScoreList) {
        this.mode = mode;
        this.roomName = roomName;
        this.userID = userID;
        this.userIDList = userIDList;
        this.userScoreList = userScoreList;
    }

    // 우승자로 인해 게임이 종료될 때의 프로토콜
    public SketchingData(int mode, String roomName, String userID, Vector<String> userIDList, Vector<Integer> userScoreList, SortedMap<String, Integer> sortedMap) {
        this.mode = mode;
        this.roomName = roomName;
        this.userID = userID;
        this.userIDList = userIDList;
        this.userScoreList = userScoreList;
        this.sortedMap = sortedMap;
    }

    // 스케치데이터 전송시 생성자
    public SketchingData(int mode, Line line, String roomName) {
        this.mode = mode;
        this.line = line;
        this.roomName = roomName;
        message = null;
    }

    // 채팅 메시지 전송시 생성자
    // 서버에서 라운드 전송시 생성자 => userID는 방 이름, message는 제시어, roomName은 화가의 userID
    public SketchingData(int mode, String userID, String message, String roomName) {
        this.mode = mode;
        this.userID = userID;
        this.message = message;
        this.roomName = roomName;
    }

    // 플레이어&점수리스트 전송용 생성자
    public SketchingData(int mode, String roomName, Vector<String> userIDList, Vector<Integer> userScoreList) {
        this.mode = mode;
        this.roomName = roomName;
        this.userIDList = userIDList;
        this.userScoreList = userScoreList;
    }

    // 클라이언트에서 서버로 방 생성, 입장 관련 생성자
    public SketchingData(int mode, String roomName, String ownerName, String IPAddress, int portNumber) {
        this.mode = mode;
        this.roomName = roomName;
        this.ownerName = ownerName;
        this.IPAddress = IPAddress;
        this.portNumber = portNumber;
    }

    // 서버에서 클라이언트로 방 생성 관련 생성자, 방 입장 관련 생성자
    public SketchingData(int mode, String roomName, String ownerName, String IPAddress, int portNumber, boolean isSuccess) {
        this.mode = mode;
        this.roomName = roomName;
        this.ownerName = ownerName;
        this.IPAddress = IPAddress;
        this.portNumber = portNumber;
        this.isSuccess = isSuccess;
    }

    // 현재 존재하는 방에 리스트들을 갖고올 때의 생성자
    public SketchingData(int mode, Vector<String> roomList, String userID, Vector<Integer> userCnt) {
        this.mode = mode;
        this.roomList = roomList;
        this.userID = userID;
        this.userCnt = userCnt;
    }

    // 클라이언트 -> 서버로 준비 완료 및 취소 요청을 보낼 때
    public SketchingData(int mode, String roomName, String userID, boolean isReady) {
        this.mode = mode;
        this.roomName = roomName;
        this.userID = userID;
        this.isReady = isReady;
    }

    // 서버 -> 클라이언트로 준비 완료 및 취소 응답을 보낼 때
    public SketchingData(int mode, String roomName, String userID, boolean isReady, boolean isSuccess) {
        this.mode = mode;
        this.roomName = roomName;
        this.userID = userID;
        this.isReady = isReady;
        this.isSuccess = isSuccess;
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

    public String getRoomName() {
        return roomName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public Vector<String> getRoomList() {
        return roomList;
    }


    public boolean isReady() {
        return isReady;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public Vector<Integer> getUserCnt() {
        return userCnt;
    }

    public SortedMap<String, Integer> getSortedMap() {
        return sortedMap;
    }
}