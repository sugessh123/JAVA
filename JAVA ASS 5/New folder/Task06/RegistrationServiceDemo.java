// Demonstrates normal, invalid and duplicate registrations, filtering, paging and export
// using the in-memory DAO (no MySQL needed).
public class RegistrationServiceDemo {
    static void reg(RegistrationService s, String id, String name, String event) {
        try {
            s.register(id, name, event);
            System.out.println("OK        " + id + " -> " + event);
        } catch (ValidationException e) {
            System.out.println("INVALID   " + e.getMessage());
        } catch (DuplicateRegistrationException e) {
            System.out.println("DUPLICATE " + e.getMessage());
        } catch (RegistrationException e) {
            System.out.println("DB ERROR  " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        RegistrationService s = new RegistrationService(new InMemoryRegistrationDao());
        reg(s, "CSA2401001", "Asha Rao", "Hackathon");          // normal
        reg(s, "CSA2401002", "Ravi Kumar", "Hackathon");        // normal
        reg(s, "CSA2401003", "Meena S", "Music Night");         // normal
        reg(s, "CSA2401001", "Asha Rao", "Hackathon");          // duplicate
        reg(s, "CSA2401001", "Asha Rao", "Music Night");        // same student, other event: allowed
        reg(s, "", "No Id", "Hackathon");                       // invalid: empty ID
        reg(s, "AB1", "Short Id", "Hackathon");                 // invalid: ID too short
        reg(s, "CSA2401004", "R2D2 99", "Hackathon");           // invalid: name
        reg(s, "CSA2401004", "Kiran P", "Chess Club");          // invalid: unknown event

        RegistrationService.Page all = s.page(null, 0, 3);
        System.out.println("All: total=" + all.total + ", pages=" + all.pageCount + ", first page rows=" + all.rows.size());
        RegistrationService.Page p2 = s.page(null, 1, 3);
        System.out.println("Page 2 rows=" + p2.rows.size());
        RegistrationService.Page hack = s.page("Hackathon", 0, 3);
        System.out.println("Hackathon only: total=" + hack.total);
        System.out.print(s.exportCsv("Hackathon"));
    }
}
