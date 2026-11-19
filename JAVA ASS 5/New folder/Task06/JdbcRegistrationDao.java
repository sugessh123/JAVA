import java.sql.*;
import java.util.*;

// MySQL implementation (the only class that knows SQL). Every statement is a PreparedStatement
// and every connection is closed by try-with-resources.
public class JdbcRegistrationDao implements RegistrationDao {
    private final String url, user, password;

    public JdbcRegistrationDao(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }

    private Connection open() throws SQLException { return DriverManager.getConnection(url, user, password); }

    @Override public List<String> events() throws DataAccessException {
        try (Connection c = open(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT name FROM events ORDER BY name")) {
            List<String> list = new ArrayList<>();
            while (rs.next()) list.add(rs.getString(1));
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load events", e);
        }
    }

    @Override public void insert(String studentId, String studentName, String eventName)
            throws DuplicateRegistrationException, DataAccessException {
        String sql = "INSERT INTO registrations (student_id, student_name, event_name) VALUES (?, ?, ?)";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, studentName);
            ps.setString(3, eventName);
            ps.executeUpdate();
        } catch (SQLException e) {
            // SQLState 23xxx = integrity constraint violation (MySQL error 1062 = duplicate key)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23") && e.getErrorCode() == 1062) {
                throw new DuplicateRegistrationException("Student " + studentId + " is already registered for " + eventName);
            }
            throw new DataAccessException("Could not save the registration", e);
        }
    }

    @Override public List<Registration> page(String eventFilter, int limit, int offset) throws DataAccessException {
        String sql = "SELECT id, student_id, student_name, event_name, registered_at FROM registrations "
                + (eventFilter == null ? "" : "WHERE event_name = ? ") + "ORDER BY id LIMIT ? OFFSET ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (eventFilter != null) ps.setString(i++, eventFilter);
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Registration> list = new ArrayList<>();
                while (rs.next()) list.add(new Registration(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not load registrations", e);
        }
    }

    @Override public int count(String eventFilter) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM registrations" + (eventFilter == null ? "" : " WHERE event_name = ?");
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (eventFilter != null) ps.setString(1, eventFilter);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not count registrations", e);
        }
    }
}
