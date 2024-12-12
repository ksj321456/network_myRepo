import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class BottomPanel extends JPanel {

    public BottomPanel() {

        ImageIcon paletteIcon = new ImageIcon("images/camera.png");
        Image paletteImage = paletteIcon.getImage(); // ImageIcon을 Image로 변환
        Image scaledPaletteImage = paletteImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH); // 이미지 크기 조절
        ImageIcon scaledPaletteIcon = new ImageIcon(scaledPaletteImage); // 다시 ImageIcon으로 변환

        JButton screenShotBtn = new JButton(scaledPaletteIcon);

        screenShotBtn.setBackground(new Color(255, 192, 203));
        screenShotBtn.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        screenShotBtn.setFont(new Font("Baloo", Font.PLAIN, 14));
        screenShotBtn.setBounds(845, 547, 80, 25);
        screenShotBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == screenShotBtn) {
                    Frame frame = new Frame("화면 저장");
                    FileDialog fd1 = new FileDialog(frame, "화면 저장", FileDialog.SAVE);
                    fd1.setVisible(true);

                    String dir = fd1.getDirectory();
                    String file = fd1.getFile();
                    String saveFileExtension = "png";
                    if (dir == null || file == null)
                        return;

                    try {
                        Robot robot = new Robot();
                        Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                        BufferedImage image = robot.createScreenCapture(rectangle);
                        File file2 = new File(dir + file + "." + saveFileExtension);
                        ImageIO.write(image, saveFileExtension, file2);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });

        add(screenShotBtn); // 게임 스크린샷 버튼 추가
    }

}