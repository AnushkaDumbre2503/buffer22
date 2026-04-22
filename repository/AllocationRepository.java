package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.AdSlot;
import model.AllocationEvent;

public class AllocationRepository {
    
    public AllocationEvent create(AllocationEvent event) throws SQLException {
        String sql = "INSERT INTO allocation_events (user_id, session_id, ad_id, slot_type, score, final_price, was_clicked) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, event.getUserId());
            stmt.setInt(2, event.getSessionId());
            stmt.setInt(3, event.getAdId());
            stmt.setString(4, event.getSlotType().name());
            stmt.setDouble(5, event.getScore());
            stmt.setDouble(6, event.getFinalPrice());
            stmt.setBoolean(7, event.getWasClicked());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating allocation event failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating allocation event failed, no ID obtained.");
                }
            }
            
            return event;
        }
    }
    
    public Optional<AllocationEvent> findById(int id) throws SQLException {
        String sql = "SELECT * FROM allocation_events WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAllocationEvent(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<AllocationEvent> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM allocation_events WHERE user_id = ? ORDER BY created_at DESC";
        List<AllocationEvent> events = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToAllocationEvent(rs));
                }
            }
        }
        
        return events;
    }
    
    public List<AllocationEvent> findBySessionId(int sessionId) throws SQLException {
        String sql = "SELECT * FROM allocation_events WHERE session_id = ? ORDER BY created_at DESC";
        List<AllocationEvent> events = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToAllocationEvent(rs));
                }
            }
        }
        
        return events;
    }
    
    public List<AllocationEvent> findByAdId(int adId) throws SQLException {
        String sql = "SELECT * FROM allocation_events WHERE ad_id = ? ORDER BY created_at DESC";
        List<AllocationEvent> events = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToAllocationEvent(rs));
                }
            }
        }
        
        return events;
    }
    
    public List<AllocationEvent> findByTimeRange(java.time.LocalDateTime startTime, 
                                                 java.time.LocalDateTime endTime) throws SQLException {
        String sql = "SELECT * FROM allocation_events WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        List<AllocationEvent> events = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToAllocationEvent(rs));
                }
            }
        }
        
        return events;
    }
    
    public List<AllocationEvent> findRecent(int minutesBack) throws SQLException {
        String sql = "SELECT * FROM allocation_events WHERE created_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE) ORDER BY created_at DESC";
        List<AllocationEvent> events = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, minutesBack);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToAllocationEvent(rs));
                }
            }
        }
        
        return events;
    }
    
    public List<AllocationEvent> getRecentEvents(int hoursBack) throws SQLException {
        String sql = "SELECT * FROM allocation_events WHERE created_at >= DATE_SUB(NOW(), INTERVAL ? HOUR) ORDER BY created_at DESC";
        List<AllocationEvent> events = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, hoursBack);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToAllocationEvent(rs));
                }
            }
        }
        
        return events;
    }
    
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM allocation_events";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        
        return 0;
    }
    
    public boolean updateClick(int eventId) throws SQLException {
        String sql = "UPDATE allocation_events SET was_clicked = TRUE WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateClickByAdAndSession(int adId, int sessionId) throws SQLException {
        String sql = "UPDATE allocation_events SET was_clicked = TRUE " +
                    "WHERE ad_id = ? AND session_id = ? AND was_clicked = FALSE " +
                    "ORDER BY created_at DESC LIMIT 1";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            stmt.setInt(2, sessionId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public int getTotalImpressions(int adId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM allocation_events WHERE ad_id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        
        return 0;
    }
    
    public int getTotalClicks(int adId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM allocation_events WHERE ad_id = ? AND was_clicked = TRUE";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        
        return 0;
    }
    
    public double getTotalRevenue(java.time.LocalDateTime startTime, 
                                java.time.LocalDateTime endTime) throws SQLException {
        String sql = "SELECT SUM(final_price) as total FROM allocation_events " +
                    "WHERE created_at BETWEEN ? AND ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        
        return 0.0;
    }
    
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(final_price) as total FROM allocation_events";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        
        return 0.0;
    }
    
    public double getCTR(int adId) throws SQLException {
        String sql = "SELECT " +
                    "SUM(CASE WHEN was_clicked THEN 1 ELSE 0 END) as clicks, " +
                    "COUNT(*) as impressions " +
                    "FROM allocation_events WHERE ad_id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int clicks = rs.getInt("clicks");
                    int impressions = rs.getInt("impressions");
                    return impressions > 0 ? (double) clicks / impressions : 0.0;
                }
            }
        }
        
        return 0.0;
    }
    
    public List<AllocationEvent> getTopPerformingSlots(int limit) throws SQLException {
        String sql = "SELECT slot_type, COUNT(*) as impressions, " +
                    "SUM(CASE WHEN was_clicked THEN 1 ELSE 0 END) as clicks, " +
                    "SUM(final_price) as revenue " +
                    "FROM allocation_events " +
                    "GROUP BY slot_type " +
                    "ORDER BY revenue DESC " +
                    "LIMIT ?";
        
        List<AllocationEvent> summary = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // This is a summary, not a full event
                    // You might want to create a separate summary class
                    // For now, we'll return a partial event
                    AllocationEvent event = new AllocationEvent();
                    event.setSlotType(AdSlot.valueOf(rs.getString("slot_type")));
                    event.setScore(rs.getDouble("revenue"));
                    summary.add(event);
                }
            }
        }
        
        return summary;
    }
    
    private AllocationEvent mapResultSetToAllocationEvent(ResultSet rs) throws SQLException {
        AllocationEvent event = new AllocationEvent();
        event.setId(rs.getInt("id"));
        event.setUserId(rs.getInt("user_id"));
        event.setSessionId(rs.getInt("session_id"));
        event.setAdId(rs.getInt("ad_id"));
        event.setSlotType(AdSlot.valueOf(rs.getString("slot_type")));
        event.setScore(rs.getDouble("score"));
        event.setFinalPrice(rs.getDouble("final_price"));
        event.setWasClicked(rs.getBoolean("was_clicked"));
        event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        return event;
    }
}
