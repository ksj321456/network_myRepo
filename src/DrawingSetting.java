import javax.swing.*;
import java.awt.*;

public class DrawingSetting extends JPanel {
    private final String[] lineColor = {"빨강", "주황", "노랑", "초록", "파랑", "보라"};
    private final String[] lineStroke = {"1px", "5px", "10px"};
    private JComboBox<String> colorBox;
    private JComboBox<String> strokeBox;
    private JButton eraser;
    private JLabel word;

    public DrawingSetting() {
        // Set layout for the panel
        setLayout(new FlowLayout());

        // Initialize components
        colorBox = new JComboBox<>(lineColor);
        strokeBox = new JComboBox<>(lineStroke);
        eraser = new JButton("지우개");
        word = new JLabel("???");

        // Add components to the panel
        add(colorBox);
        add(strokeBox);
        add(eraser);
        add(word);
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
