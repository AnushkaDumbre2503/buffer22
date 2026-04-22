package manager;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Session;
import repository.UserRepository;

public class SessionManager {
    private UserRepository userRepository;
    
    public SessionManager() {
        this.userRepository = new UserRepository();
    }
    
    public Session createSession(int userId) throws SQLException {
        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        Session session = new Session(userId);
        session.setId(generateSessionId());
        
        // Save to database
        String sql = "INSERT INTO user_sessions (user_id, session_token, created_at, expires_at, is_active) VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR), TRUE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, session.getSessionToken());
            stmt.executeUpdate();
        }
        
        return session;
    }
    
    public Optional<Session> getSessionByToken(String sessionToken) throws SQLException {
        String sql = "SELECT * FROM user_sessions WHERE session_token = ? AND is_active = TRUE AND expires_at > NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sessionToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Session session = new Session(rs.getInt("user_id"));
                    session.setId(rs.getInt("id"));
                    return Optional.of(session);
                }
            }
        }
        return Optional.empty();
    }
    
    public Optional<Session> getSessionById(int sessionId) throws SQLException {
        String sql = "SELECT * FROM user_sessions WHERE id = ? AND is_active = TRUE AND expires_at > NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Session session = new Session(rs.getInt("user_id"));
                    session.setId(rs.getInt("id"));
                    return Optional.of(session);
                }
            }
        }
        return Optional.empty();
    }
    
    public List<Session> getSessionsByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM user_sessions WHERE user_id = ? AND is_active = TRUE";
        List<Session> userSessions = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Session session = new Session(userId);
                    session.setId(rs.getInt("id"));
                    userSessions.add(session);
                }
            }
        }
        return userSessions;
    }
    
    public boolean invalidateSession(String sessionToken) throws SQLException {
        String sql = "UPDATE user_sessions SET is_active = FALSE WHERE session_token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sessionToken);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean invalidateSession(int sessionId) throws SQLException {
        String sql = "UPDATE user_sessions SET is_active = FALSE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sessionId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public void invalidateUserSessions(int userId) throws SQLException {
        String sql = "UPDATE user_sessions SET is_active = FALSE WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    public void cleanupExpiredSessions() throws SQLException {
        String sql = "UPDATE user_sessions SET is_active = FALSE WHERE expires_at <= NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.executeUpdate();
        }
    }
    
    public boolean extendSession(String sessionToken, int hours) throws SQLException {
        String sql = "UPDATE user_sessions SET expires_at = DATE_ADD(expires_at, INTERVAL ? HOUR) WHERE session_token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, hours);
            stmt.setString(2, sessionToken);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public int getActiveSessionCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_sessions WHERE is_active = TRUE AND expires_at > NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // For demo purposes - get or create session for user
    public Session getOrCreateDemoSession(int userId) throws SQLException {
        List<Session> userSessions = getSessionsByUserId(userId);
        
        // Find an active, non-expired session
        if (!userSessions.isEmpty()) {
            return userSessions.get(0);
        }
        
        // Create new session
        return createSession(userId);
    }
    
    private int generateSessionId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }
}
