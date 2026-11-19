import java.util.List;

// Data-access layer contract. The service layer depends on this interface, not on JDBC.
public interface RegistrationDao {
    List<String> events() throws DataAccessException;
    void insert(String studentId, String studentName, String eventName)
            throws DuplicateRegistrationException, DataAccessException;
    List<Registration> page(String eventFilter, int limit, int offset) throws DataAccessException;   // eventFilter null = all
    int count(String eventFilter) throws DataAccessException;
}
