package Buttons;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScreenShotBtn extends TemplateButton {

    public ScreenShotBtn() {
        super("images/camera.png", new Color(215, 179, 246));
    }

    @Override
    protected ActionListener getActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Frame frame = new Frame("한성 스케치: 스크린샷 저장");
                FileDialog fileDialog = new FileDialog(frame, "한성 스케치: 스크린샷 저장", FileDialog.SAVE); // 스크린샷 저장 다이얼로그 생성
                fileDialog.setVisible(true);

                String path = fileDialog.getDirectory(); // 사용자가 선택한 스크린샷이 저장될 디렉터리 경로
                String fileName = fileDialog.getFile(); // 사용자가 입력한 스크린샷 파일 이름
                String fileExtension = "png"; // 스크린샷 파일 확장자
                if (path == null || fileName == null) {
                    return;
                }

                try {
                    Robot screenShotRobot = new Robot(); // 화면 캡처 작업 자동화를 위한 Robot 객체 생성

                    Rectangle screenShotArea = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()); // 캡처할 화면 영역 설정: 모니터 화면 전체
                    BufferedImage finalImage = screenShotRobot.createScreenCapture(screenShotArea); // Robot 객체의 createScreenCapture() 메서드를 사용하여 지정된 영역(screenShotArea)의 스크린샷을 캡처하고 BufferedImage 객체에 저장
                    File fullFilePath = new File(path + fileName + "." + fileExtension); // 저장할 스크린샷 파일의 최종경로
                    ImageIO.write(finalImage, fileExtension, fullFilePath);

                    // 스크린샷 저장 성공 후 안내 창 표시
                    JOptionPane.showMessageDialog(null,
                            fullFilePath + "경로에 스크린샷을 저장했습니다!",
                            "스크린샷 저장 완료",
                            JOptionPane.INFORMATION_MESSAGE);

                } catch (AWTException ex) {
                    // 스크린샷 저장 실패 시 오류 다이얼로그 표시
                    JOptionPane.showMessageDialog(null,
                            "스크린샷 저장에 실패하였습니다!",
                            "오류",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } catch (IOException ex) {
                    // 스크린샷 저장 실패 시 오류 다이얼로그 표시
                    JOptionPane.showMessageDialog(null,
                            "스크린샷 저장에 실패하였습니다!",
                            "오류",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        };
    }
}
