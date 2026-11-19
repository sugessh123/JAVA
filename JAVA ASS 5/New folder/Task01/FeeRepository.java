import java.sql.SQLException;

// Data-access contract. The service only knows this interface, not JDBC.
public interface FeeRepository {
    void save(String regNo, double amount) throws SQLException;
}
