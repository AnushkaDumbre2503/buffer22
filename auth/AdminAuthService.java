package auth;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AdminAuthService implements IAuthService {
    
    @Override
    public AuthContext login(String username, String password) throws SQLException {
        String sql = "SELECT id FROM system_officers WHERE username = ? AND password = SHA2(?, 256)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int adminId = rs.getInt("id");
                    String sessionToken = UUID.randomUUID().toString();
                    return new AuthContext(adminId, username, RoleType.ADMIN, sessionToken);
                }
            }
        }
        return null;
    }
    
    @Override
    public boolean signup(String username, String email, String password) throws SQLException {
        String sql = "INSERT INTO system_officers (username, email, password) VALUES (?, ?, SHA2(?, 256))";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    
    @Override
    public boolean validateSession(String sessionToken) throws SQLException {
        return sessionToken != null && !sessionToken.isEmpty();
    }
}
