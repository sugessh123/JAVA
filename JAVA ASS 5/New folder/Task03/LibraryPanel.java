import javax.swing.*;
import java.awt.*;

// The whole library search screen. It is a JPanel so that it can be placed in a JFrame
// (LibrarySearchScreen) or drawn off-screen for the resizing screenshots.
public class LibraryPanel extends JPanel {
    private static final String[] BOOKS = {
        "Java: The Complete Reference", "Effective Java", "Head First Java", "Clean Code",
        "Database System Concepts", "Operating System Concepts", "Computer Networks",
        "Data Structures in Java", "Introduction to Algorithms", "Design Patterns",
        "Refactoring", "Discrete Mathematics"
    };
    private static final int PAGE = 4;

    private final JTextField query = new JTextField(20);
    private final JComboBox<String> category = new JComboBox<>(new String[] { "All", "Programming", "Systems", "Theory" });
    private final JButton search = new JButton("Search");
    private final JTextArea results = new JTextArea();
    private final JButton prev = new JButton("Prev");
    private final JButton next = new JButton("Next");
    private final JButton reserve = new JButton("Reserve");
    private final JLabel status = new JLabel("Enter a title and press Search");
    private java.util.List<String> found = new java.util.ArrayList<>();
    private int page = 0;

    public LibraryPanel() {
        setLayout(new BorderLayout(8, 8));                         // root: NORTH / CENTER / SOUTH
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // NORTH: two rows (title bar, search row) stacked with GridLayout(2,1)
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel title = new JLabel("Campus Library Search");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        titleBar.add(title);
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(new JLabel("Search:"));
        searchRow.add(query);
        searchRow.add(category);
        searchRow.add(search);
        JPanel north = new JPanel(new GridLayout(2, 1));
        north.add(titleBar);
        north.add(searchRow);
        add(north, BorderLayout.NORTH);

        // CENTER: results area, it takes all the extra space
        results.setEditable(false);
        add(new JScrollPane(results), BorderLayout.CENTER);

        // SOUTH: button row and status message
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonRow.add(prev);
        buttonRow.add(next);
        buttonRow.add(reserve);
        JPanel south = new JPanel(new GridLayout(2, 1));
        south.add(buttonRow);
        south.add(status);
        add(south, BorderLayout.SOUTH);

        // Handlers
        search.addActionListener(e -> doSearch());
        query.addActionListener(e -> doSearch());
        prev.addActionListener(e -> { if (page > 0) { page--; showPage(); } });
        next.addActionListener(e -> { if ((page + 1) * PAGE < found.size()) { page++; showPage(); } });
        reserve.addActionListener(e -> status.setText(found.isEmpty() ? "Nothing to reserve" : "Reserved the first book on this page"));
    }

    void doSearch() {
        String q = query.getText().trim().toLowerCase();
        found = new java.util.ArrayList<>();
        for (String b : BOOKS) if (b.toLowerCase().contains(q)) found.add(b);
        page = 0;
        showPage();
    }

    void showPage() {
        StringBuilder sb = new StringBuilder();
        for (int i = page * PAGE; i < Math.min(found.size(), (page + 1) * PAGE); i++) sb.append(i + 1).append(". ").append(found.get(i)).append('\n');
        results.setText(sb.toString());
        status.setText(found.size() + " result(s), page " + (page + 1));
    }

    // Used only by the test code
    JTextField queryField() { return query; }
    void runSearch() { doSearch(); }
}
