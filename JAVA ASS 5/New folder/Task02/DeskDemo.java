import java.util.List;

// Tests the doctor map and the booking rules without opening a window.
public class DeskDemo {
    public static void main(String[] args) {
        DoctorDirectory dir = new DoctorDirectory();
        AppointmentService svc = new AppointmentService();
        for (String dep : dir.departments()) System.out.println(dep + " -> " + dir.doctorsIn(dep));
        System.out.println("Unknown department -> " + dir.doctorsIn("Dermatology"));

        Doctor rao = dir.doctorsIn("Cardiology").get(0);
        System.out.println(svc.book("123456", "Cardiology", rao, "09:00"));    // normal
        System.out.println(svc.book("123456", "Cardiology", rao, "09:00"));    // same doctor, same slot
        System.out.println(svc.book("12", "Cardiology", rao, "10:00"));        // bad patient ID
        System.out.println(svc.book("123456", null, rao, "10:00"));            // no department
        System.out.println(svc.book("123456", "Cardiology", null, "10:00"));   // no doctor
        System.out.println(svc.book("654321", "Cardiology", rao, "10:00"));    // different slot, fine
    }
}
