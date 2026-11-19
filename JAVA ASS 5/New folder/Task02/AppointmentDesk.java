import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// Outpatient appointment window.
public class AppointmentDesk extends JFrame {
    private final DoctorDirectory directory = new DoctorDirectory();
    private final AppointmentService service = new AppointmentService();

    private final JTextField patientId = new JTextField(10);
    private final JComboBox<String> department = new JComboBox<>(directory.departments().toArray(new String[0]));
    private final JComboBox<Doctor> doctor = new JComboBox<>();
    private final JComboBox<String> slot = new JComboBox<>(new String[] { "09:00", "09:30", "10:00", "10:30", "11:00" });
    private final JButton book = new JButton("Book");
    private final JLabel message = new JLabel(" ");

    public AppointmentDesk() {
        super("Outpatient Appointment Desk");
        setLayout(new GridLayout(6, 2, 4, 4));
        add(new JLabel("Patient ID:"));  add(patientId);
        add(new JLabel("Department:"));  add(department);
        add(new JLabel("Doctor:"));      add(doctor);
        add(new JLabel("Time slot:"));   add(slot);
        add(book);                       add(message);

        // Department changes -> refresh the doctor list (ItemListener / ItemEvent)
        department.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) refreshDoctors();
        });
        refreshDoctors();

        // Book button -> ActionListener / ActionEvent
        book.addActionListener(e -> {
            message.setText(service.book(patientId.getText(), (String) department.getSelectedItem(),
                    (Doctor) doctor.getSelectedItem(), (String) slot.getSelectedItem()));
        });

        // Window events: ask before closing if the form has been started
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                boolean inProgress = !patientId.getText().trim().isEmpty();
                int choice = inProgress
                        ? JOptionPane.showConfirmDialog(AppointmentDesk.this, "A booking is in progress. Close anyway?", "Confirm", JOptionPane.YES_NO_OPTION)
                        : JOptionPane.YES_OPTION;
                if (choice == JOptionPane.YES_OPTION) dispose();
            }
        });
        pack();
    }

    private void refreshDoctors() {
        doctor.removeAllItems();
        for (Doctor d : directory.doctorsIn((String) department.getSelectedItem())) doctor.addItem(d);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppointmentDesk().setVisible(true));
    }
}
