package ikayaki;

import javax.swing.*;
import java.awt.*;

/**
 * Kokeillaan kuinka proton layoutin tekeminen onnistuu IDEAlla...
 *
 * @author Esko Luontola
 */
public class GUITest extends JFrame {
    private JPanel contentPane;
    private JButton button8;
    private JTextField textField4;
    private JRadioButton radioButton4;
    private JRadioButton radioButton3;
    private JTable table3;
    private JTable table2;
    private JTable table1;
    private JButton button6;
    private JButton button5;
    private JRadioButton radioButton1;
    private JRadioButton radioButton2;
    private JButton button4;
    private JButton button3;
    private JButton button2;
    private JButton button1;
    private JComboBox comboBox1;
    private JButton button7;
    private JTextField textField3;
    private JTextField textField2;
    private JTextField textField1;

    public GUITest() throws HeadlessException {
        setLayout(new BorderLayout());
        add(contentPane, "Center");
        pack();

    }

    public static void main(String[] args) {
        GUITest t = new GUITest();
        t.setVisible(true);
    }
}
