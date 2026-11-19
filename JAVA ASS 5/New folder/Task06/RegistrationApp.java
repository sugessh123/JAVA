import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

// User-interface layer. It only collects input, shows results and starts background work.
// Usage: java -cp .:mysql-connector-j.jar RegistrationApp <jdbc-url> <user> <password>
public class RegistrationApp extends JFrame {
    private static final int PAGE_SIZE = 20;
    private final RegistrationService service;

    private final JTextField studentId = new JTextField(12);
    private final JTextField studentName = new JTextField(18);
    private final JComboBox<String> eventBox = new JComboBox<>();
    private final JButton register = new JButton("Register");
    private final JComboBox<String> filterBox = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(new String[] { "ID", "Student ID", "Name", "Event", "Registered at" }, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JButton prev = new JButton("Prev");
    private final JButton next = new JButton("Next");
    private final JLabel pageLabel = new JLabel("Page 1");
    private final JLabel status = new JLabel(" ");
    private int pageIndex = 0;
    private int pageCount = 1;

    public RegistrationApp(RegistrationService service) {
        super("Campus Event Registration");
        this.service = service;

        // ----- Menu bar
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem export = new JMenuItem("Export CSV...");
        JMenuItem exit = new JMenuItem("Exit");
        file.add(export); file.addSeparator(); file.add(exit);
        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        help.add(about);
        bar.add(file); bar.add(help);
        setJMenuBar(bar);

        // ----- Form (NORTH), table (CENTER), paging + status (SOUTH)
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Student ID:")); form.add(studentId);
        form.add(new JLabel("Name:")); form.add(studentName);
        form.add(new JLabel("Event:")); form.add(eventBox);
        form.add(register);
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterRow.add(new JLabel("Show event:")); filterRow.add(filterBox);
        JPanel north = new JPanel(new GridLayout(2, 1));
        north.add(form); north.add(filterRow);

        JPanel paging = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paging.add(prev); paging.add(pageLabel); paging.add(next);
        JPanel south = new JPanel(new GridLayout(2, 1));
        south.add(paging); south.add(status);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        // ----- Handlers
        register.addActionListener(e -> doRegister());
        filterBox.addActionListener(e -> { pageIndex = 0; loadPage(); });
        prev.addActionListener(e -> { if (pageIndex > 0) { pageIndex--; loadPage(); } });
        next.addActionListener(e -> { if (pageIndex + 1 < pageCount) { pageIndex++; loadPage(); } });
        export.addActionListener(e -> doExport());
        exit.addActionListener(e -> dispose());
        about.addActionListener(e -> JOptionPane.showMessageDialog(this, "Campus Event Registration\nCO4 Assignment"));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 480);
        setLocationRelativeTo(null);
        loadEvents();
    }

    private String currentFilter() {
        String s = (String) filterBox.getSelectedItem();
        return (s == null || s.equals("All events")) ? null : s;
    }

    // Each database call runs in a SwingWorker so the window stays responsive.
    private void loadEvents() {
        new SwingWorker<java.util.List<String>, Void>() {
            @Override protected java.util.List<String> doInBackground() throws Exception { return service.events(); }
            @Override protected void done() {
                try {
                    java.util.List<String> events = get();
                    eventBox.removeAllItems();
                    filterBox.removeAllItems();
                    filterBox.addItem("All events");
                    for (String ev : events) { eventBox.addItem(ev); filterBox.addItem(ev); }
                } catch (ExecutionException ex) {
                    status.setText("Error: " + ex.getCause().getMessage());
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
        }.execute();
    }

    private void doRegister() {
        register.setEnabled(false);
        final String id = studentId.getText(), name = studentName.getText(), ev = (String) eventBox.getSelectedItem();
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception { service.register(id, name, ev); return null; }
            @Override protected void done() {
                try {
                    get();
                    status.setText("Registered " + id.trim() + " for " + ev);
                    studentId.setText(""); studentName.setText("");
                    loadPage();
                } catch (ExecutionException ex) {
                    // validation, duplicate and database errors all end up here as a clear message
                    status.setText("Error: " + ex.getCause().getMessage());
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                register.setEnabled(true);
            }
        }.execute();
    }

    private void loadPage() {
        status.setText("Loading...");
        final String filter = currentFilter();
        final int index = pageIndex;
        new SwingWorker<RegistrationService.Page, Void>() {
            @Override protected RegistrationService.Page doInBackground() throws Exception { return service.page(filter, index, PAGE_SIZE); }
            @Override protected void done() {
                try {
                    RegistrationService.Page p = get();
                    model.setRowCount(0);
                    for (Registration r : p.rows) model.addRow(new Object[] { r.id, r.studentId, r.studentName, r.eventName, r.registeredAt });
                    pageCount = p.pageCount;
                    pageLabel.setText("Page " + (p.pageIndex + 1) + " of " + p.pageCount + " (" + p.total + " rows)");
                    prev.setEnabled(p.pageIndex > 0);
                    next.setEnabled(p.pageIndex + 1 < p.pageCount);
                    status.setText("Loaded " + p.rows.size() + " rows");
                } catch (ExecutionException ex) {
                    status.setText("Error: " + ex.getCause().getMessage());
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
        }.execute();
    }

    private void doExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("registrations.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        final File target = chooser.getSelectedFile();
        final String filter = currentFilter();
        status.setText("Exporting...");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                try (FileWriter w = new FileWriter(target)) { w.write(service.exportCsv(filter)); }
                return null;
            }
            @Override protected void done() {
                try { get(); status.setText("Exported to " + target.getName()); }
                catch (ExecutionException ex) { status.setText("Export failed: " + ex.getCause().getMessage()); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
        }.execute();
    }

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : "jdbc:mysql://localhost:3306/campus_events";
        String user = args.length > 1 ? args[1] : "root";
        String pwd = args.length > 2 ? args[2] : "password";
        RegistrationService service = new RegistrationService(new JdbcRegistrationDao(url, user, pwd));
        SwingUtilities.invokeLater(() -> {
            RegistrationApp app = new RegistrationApp(service);
            app.setVisible(true);
            app.loadPage();
        });
    }
}
