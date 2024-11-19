import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DrawingSetting extends JPanel {
    private final String[] lineColor = {"검정", "빨강", "주황", "노랑", "초록", "파랑", "보라"};
    private final String[] lineStroke = {"1px", "5px", "10px"};
    private JComboBox<String> colorBox;
    private JComboBox<String> strokeBox;
    private JButton eraser;
    private JLabel word;

    public DrawingSetting() {
        setLayout(new FlowLayout());

        colorBox = new JComboBox<>(lineColor);
        strokeBox = new JComboBox<>(lineStroke);
        eraser = new JButton("지우개");
        word = new JLabel("???");

        add(colorBox);
        add(strokeBox);
        add(eraser);
        add(word);
    }

    public Color getSelectedColor() {
        String selectedColor = (String) colorBox.getSelectedItem();
        switch (selectedColor) {
            case "검정":
                return Color.BLACK;
            case "빨강":
                return Color.RED;
            case "주황":
                return Color.ORANGE;
            case "노랑":
                return Color.YELLOW;
            case "초록":
                return Color.GREEN;
            case "파랑":
                return Color.BLUE;
            case "보라":
                return Color.MAGENTA;
            default:
                return Color.BLACK; // 기본 색상
        }
    }

    public float getSelectedLineWidth() {
        String selectedWidth = (String) strokeBox.getSelectedItem();
        switch (selectedWidth) {
            case "1px":
                return 1;
            case "5px":
                return 5;
            case "10px":
                return 10;
            // 기본 굵기 1
            default:
                return 1;
        }
    }

    public String[] getLineColor() {
        return lineColor;
    }

    public String[] getLineStroke() {
        return lineStroke;
    }

    public JComboBox<String> getColorBox() {
        return colorBox;
    }

    public void setColorBox(JComboBox<String> colorBox) {
        this.colorBox = colorBox;
    }

    public JComboBox<String> getStrokeBox() {
        return strokeBox;
    }

    public void setStrokeBox(JComboBox<String> strokeBox) {
        this.strokeBox = strokeBox;
    }

    public JButton getEraser() {
        return eraser;
    }

    public void setEraser(JButton eraser) {
        this.eraser = eraser;
    }

    public JLabel getWord() {
        return word;
    }

    public void setWord(JLabel word) {
        this.word = word;
    }
}
