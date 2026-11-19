import java.util.*;

// Service layer: validation rules, duplicate handling, paging and CSV export.
// It never touches Swing or SQL.
public class RegistrationService {
    private final RegistrationDao dao;

    public RegistrationService(RegistrationDao dao) { this.dao = dao; }

    public List<String> events() throws RegistrationException { return dao.events(); }

    public void register(String studentId, String name, String event) throws RegistrationException {
        if (studentId == null || studentId.trim().isEmpty()) throw new ValidationException("Student ID is required");
        if (!studentId.trim().matches("[A-Za-z0-9]{6,15}")) throw new ValidationException("Student ID must be 6-15 letters or digits");
        if (name == null || name.trim().isEmpty()) throw new ValidationException("Name is required");
        if (!name.trim().matches("[A-Za-z][A-Za-z .]{1,59}")) throw new ValidationException("Name must be 2-60 letters, spaces or dots");
        if (event == null || !dao.events().contains(event)) throw new ValidationException("Select a valid event");
        // The database UNIQUE constraint is the final guard against duplicates (safe even with two desks).
        dao.insert(studentId.trim(), name.trim(), event);
    }

    public static class Page {
        public final List<Registration> rows; public final int total; public final int pageIndex; public final int pageCount;
        Page(List<Registration> rows, int total, int pageIndex, int pageSize) {
            this.rows = rows; this.total = total; this.pageIndex = pageIndex;
            this.pageCount = Math.max(1, (total + pageSize - 1) / pageSize);
        }
    }

    public Page page(String eventFilter, int pageIndex, int pageSize) throws RegistrationException {
        if (pageIndex < 0) pageIndex = 0;
        return new Page(dao.page(eventFilter, pageSize, pageIndex * pageSize), dao.count(eventFilter), pageIndex, pageSize);
    }

    // Export: reads the data in blocks of 500 so memory use stays small.
    public String exportCsv(String eventFilter) throws RegistrationException {
        StringBuilder sb = new StringBuilder("id,student_id,student_name,event_name,registered_at\n");
        int offset = 0;
        while (true) {
            List<Registration> block = dao.page(eventFilter, 500, offset);
            if (block.isEmpty()) break;
            for (Registration r : block)
                sb.append(r.id).append(',').append(csv(r.studentId)).append(',').append(csv(r.studentName)).append(',')
                  .append(csv(r.eventName)).append(',').append(csv(r.registeredAt)).append('\n');
            offset += block.size();
        }
        return sb.toString();
    }

    private static String csv(String s) {
        return (s.contains(",") || s.contains("\"")) ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }
}
