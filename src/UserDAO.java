import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {
    // register user
    public boolean register(User user) {
        String sql = "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            int affected = ps.executeUpdate();
            if (affected == 0) return false;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) user.setUserId(rs.getInt(1));
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    // login: returns User or null
    public User login(String email, String passwordPlain) {
        String sql = "SELECT user_id, name, email, password_hash FROM users WHERE email = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (hash.equals(Utils.sha256(passwordPlain))) {
                        User u = new User();
                        u.setUserId(rs.getInt("user_id"));
                        u.setName(rs.getString("name"));
                        u.setEmail(rs.getString("email"));
                        u.setPasswordHash(hash);
                        return u;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }
}
