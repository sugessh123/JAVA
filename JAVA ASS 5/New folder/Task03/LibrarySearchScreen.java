import javax.swing.*;

public class LibrarySearchScreen extends JFrame {
    public LibrarySearchScreen() {
        super("Library Search");
        setContentPane(new LibraryPanel());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySearchScreen().setVisible(true));
    }
}
