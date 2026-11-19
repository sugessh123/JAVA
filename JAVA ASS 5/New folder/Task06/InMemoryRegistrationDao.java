import java.util.*;

// Test double with the same behaviour as the database (including the duplicate rule).
public class InMemoryRegistrationDao implements RegistrationDao {
    private final List<String> events = Arrays.asList("Music Night", "Hackathon", "Dance Fest", "Quiz Bowl");
    private final List<Registration> rows = new ArrayList<>();
    private final Set<String> keys = new HashSet<>();

    @Override public List<String> events() { return events; }

    @Override public void insert(String studentId, String studentName, String eventName) throws DuplicateRegistrationException {
        if (!keys.add(studentId + "|" + eventName))
            throw new DuplicateRegistrationException("Student " + studentId + " is already registered for " + eventName);
        rows.add(new Registration(rows.size() + 1, studentId, studentName, eventName, "now"));
    }

    @Override public List<Registration> page(String eventFilter, int limit, int offset) {
        List<Registration> match = new ArrayList<>();
        for (Registration r : rows) if (eventFilter == null || r.eventName.equals(eventFilter)) match.add(r);
        if (offset >= match.size()) return new ArrayList<>();
        return new ArrayList<>(match.subList(offset, Math.min(match.size(), offset + limit)));
    }

    @Override public int count(String eventFilter) {
        int n = 0;
        for (Registration r : rows) if (eventFilter == null || r.eventName.equals(eventFilter)) n++;
        return n;
    }
}
