import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConverterA extends JFrame implements ActionListener {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> from = new JComboBox<>(new String[] { "INR", "USD", "EUR" });
    private final JComboBox<String> to = new JComboBox<>(new String[] { "USD", "EUR", "INR" });
    private final JButton convert = new JButton("Convert");
    private final JLabel result = new JLabel(" ");

    public ConverterA() {
        super("Converter A - frame implements ActionListener");
        setLayout(new FlowLayout());
        add(amount); add(from); add(new JLabel("to")); add(to); add(convert); add(result);
        convert.addActionListener(this);
        amount.addActionListener(this);   // Enter key
        // Window closing is handled with a WindowAdapter in all four styles
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); System.exit(0); }
        });
        pack();
    }

    @Override public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(this, ConverterLogic.convert(amount.getText(), (String) from.getSelectedItem(), (String) to.getSelectedItem()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConverterA().setVisible(true));
    }
}
