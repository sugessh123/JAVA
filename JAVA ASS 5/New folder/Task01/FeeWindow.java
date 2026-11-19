import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

// The window only collects input and shows results. Every button has its own small listener.
public class FeeWindow extends JFrame {
    private final JTextField regNo = new JTextField(12);
    private final JTextField amount = new JTextField(12);
    private final JButton pay = new JButton("Pay");
    private final JButton clear = new JButton("Clear");
    private final JButton receipt = new JButton("Receipt");
    private final JLabel message = new JLabel(" ");
    private final FeeService service;

    public FeeWindow(FeeService service) {
        super("Fee Payment");
        this.service = service;
        setLayout(new GridLayout(4, 1, 4, 4));
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT)); row1.add(new JLabel("Register No:")); row1.add(regNo);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT)); row2.add(new JLabel("Amount (Rs):")); row2.add(amount);
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.CENTER)); row3.add(pay); row3.add(clear); row3.add(receipt);
        add(row1); add(row2); add(row3); add(message);

        pay.addActionListener(e -> doPay());
        clear.addActionListener(e -> { regNo.setText(""); amount.setText(""); message.setText(" "); });
        receipt.addActionListener(e -> showReceipt());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    private void doPay() {
        pay.setEnabled(false);
        message.setText("Waiting for bank confirmation...");
        final String r = regNo.getText(), a = amount.getText();
        // Long work runs in a background thread so the window never freezes.
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { return service.pay(r, a); }
            @Override protected void done() {
                try {
                    message.setText(get());
                } catch (ExecutionException ex) {
                    message.setText("Error: " + ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                pay.setEnabled(true);
            }
        }.execute();
    }

    private void showReceipt() {
        try {
            JOptionPane.showMessageDialog(this, service.lastReceipt());
        } catch (FeeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No receipt", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Replace URL, USER and PASSWORD with your MySQL details.
        FeeRepository repo = new JdbcFeeRepository("jdbc:mysql://localhost:3306/college", "root", "password");
        SwingUtilities.invokeLater(() -> new FeeWindow(new FeeService(repo, 3000)).setVisible(true));
    }
}
