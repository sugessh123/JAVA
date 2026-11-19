import java.sql.*;

// Real database version. PreparedStatement stops SQL injection and try-with-resources always
// closes the connection, even when an exception occurs.
public class JdbcFeeRepository implements FeeRepository {
    private final String url, user, password;

    public JdbcFeeRepository(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }

    @Override
    public void save(String regNo, double amount) throws SQLException {
        String sql = "INSERT INTO fees (reg_no, amount) VALUES (?, ?)";
        try (Connection c = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, regNo);
            ps.setDouble(2, amount);
            ps.executeUpdate();
        }
    }
}
