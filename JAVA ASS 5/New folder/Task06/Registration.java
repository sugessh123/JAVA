public class Registration {
    final int id;
    final String studentId;
    final String studentName;
    final String eventName;
    final String registeredAt;

    Registration(int id, String studentId, String studentName, String eventName, String registeredAt) {
        this.id = id; this.studentId = studentId; this.studentName = studentName;
        this.eventName = eventName; this.registeredAt = registeredAt;
    }
}
