import java.util.*;

// Validation and booking rules. No GUI classes are used here.
public class AppointmentService {
    private final Set<String> booked = new HashSet<>();

    public String book(String patientId, String department, Doctor doctor, String slot) {
        if (patientId == null || !patientId.trim().matches("\\d{5,8}")) return "Error: patient ID must be 5 to 8 digits";
        if (department == null) return "Error: select a department";
        if (doctor == null) return "Error: select a doctor";
        if (slot == null) return "Error: select a time slot";
        String key = doctor.name + "|" + slot;
        if (!booked.add(key)) return "Error: " + doctor.name + " is already booked at " + slot;
        return "Booked: patient " + patientId.trim() + " with " + doctor.name + " at " + slot;
    }
}
