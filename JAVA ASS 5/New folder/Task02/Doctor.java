public class Doctor {
    final String name;
    final String department;
    Doctor(String name, String department) { this.name = name; this.department = department; }
    @Override public String toString() { return name; }
}
