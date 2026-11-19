import java.util.*;

// Doctors are loaded ONCE into a Map<department, list of doctors>. Changing the department
// then only needs a map lookup, not a database query.
public class DoctorDirectory {
    private final Map<String, List<Doctor>> byDepartment = new LinkedHashMap<>();

    public DoctorDirectory() {
        add("Cardiology", "Dr. Rao", "Dr. Iyer");
        add("Orthopaedics", "Dr. Khan", "Dr. Thomas", "Dr. Nair");
        add("Paediatrics", "Dr. Mehta");
    }

    private void add(String department, String... names) {
        List<Doctor> list = new ArrayList<>();
        for (String n : names) list.add(new Doctor(n, department));
        byDepartment.put(department, list);
    }

    public Set<String> departments() { return byDepartment.keySet(); }

    public List<Doctor> doctorsIn(String department) {
        List<Doctor> list = byDepartment.get(department);
        return list == null ? Collections.<Doctor>emptyList() : list;
    }
}
