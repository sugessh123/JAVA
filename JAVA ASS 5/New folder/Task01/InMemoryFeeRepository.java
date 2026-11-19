import java.util.*;

// Test double: stores payments in a list, so the service can be tested with no database.
public class InMemoryFeeRepository implements FeeRepository {
    final List<String> rows = new ArrayList<>();
    boolean failNext = false;

    @Override
    public void save(String regNo, double amount) throws java.sql.SQLException {
        if (failNext) { failNext = false; throw new java.sql.SQLException("Database is down"); }
        rows.add(regNo + ":" + amount);
    }
}
