package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Advertiser;

public class AdvertiserRepository {
    
    public Advertiser create(Advertiser advertiser) throws SQLException {
        String sql = "INSERT INTO advertisers (name, total_budget, remaining_budget) VALUES (?, ?, ?)";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, advertiser.getName());
            stmt.setDouble(2, advertiser.getTotalBudget());
            stmt.setDouble(3, advertiser.getRemainingBudget());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating advertiser failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    advertiser.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating advertiser failed, no ID obtained.");
                }
            }
            
            return advertiser;
        }
    }
    
    public Optional<Advertiser> findById(int id) throws SQLException {
        String sql = "SELECT * FROM advertisers WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAdvertiser(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<Advertiser> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM advertisers WHERE name = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAdvertiser(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Advertiser> findAll() throws SQLException {
        String sql = "SELECT * FROM advertisers ORDER BY created_at DESC";
        List<Advertiser> advertisers = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                advertisers.add(mapResultSetToAdvertiser(rs));
            }
        }
        
        return advertisers;
    }
    
    public List<Advertiser> findWithBudget() throws SQLException {
        String sql = "SELECT * FROM advertisers WHERE remaining_budget > 0 ORDER BY remaining_budget DESC";
        List<Advertiser> advertisers = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                advertisers.add(mapResultSetToAdvertiser(rs));
            }
        }
        
        return advertisers;
    }
    
    public boolean update(Advertiser advertiser) throws SQLException {
        String sql = "UPDATE advertisers SET name = ?, total_budget = ?, remaining_budget = ? WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, advertiser.getName());
            stmt.setDouble(2, advertiser.getTotalBudget());
            stmt.setDouble(3, advertiser.getRemainingBudget());
            stmt.setInt(4, advertiser.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateBudget(int advertiserId, double remainingBudget) throws SQLException {
        String sql = "UPDATE advertisers SET remaining_budget = ? WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, remainingBudget);
            stmt.setInt(2, advertiserId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM advertisers WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public void loadConflicts(Advertiser advertiser) throws SQLException {
        String sql = "SELECT advertiser2_id FROM advertiser_conflicts WHERE advertiser1_id = ? " +
                    "UNION SELECT advertiser1_id FROM advertiser_conflicts WHERE advertiser2_id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, advertiser.getId());
            stmt.setInt(2, advertiser.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    advertiser.addConflictingAdvertiser(rs.getInt(1));
                }
            }
        }
    }
    
    public void addConflict(int advertiser1Id, int advertiser2Id) throws SQLException {
        String sql = "INSERT IGNORE INTO advertiser_conflicts (advertiser1_id, advertiser2_id) VALUES (?, ?)";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, advertiser1Id);
            stmt.setInt(2, advertiser2Id);
            stmt.executeUpdate();
        }
    }
    
    public void removeConflict(int advertiser1Id, int advertiser2Id) throws SQLException {
        String sql = "DELETE FROM advertiser_conflicts WHERE " +
                    "(advertiser1_id = ? AND advertiser2_id = ?) OR " +
                    "(advertiser1_id = ? AND advertiser2_id = ?)";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, advertiser1Id);
            stmt.setInt(2, advertiser2Id);
            stmt.setInt(3, advertiser2Id);
            stmt.setInt(4, advertiser1Id);
            stmt.executeUpdate();
        }
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
    
    public int getActiveAdvertisersCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM advertisers WHERE remaining_budget > 0";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        
        return 0;
    }
    
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM advertisers";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        
        return 0;
    }
    
    private Advertiser mapResultSetToAdvertiser(ResultSet rs) throws SQLException {
        Advertiser advertiser = new Advertiser();
        advertiser.setId(rs.getInt("id"));
        advertiser.setName(rs.getString("name"));
        advertiser.setTotalBudget(rs.getDouble("total_budget"));
        advertiser.setRemainingBudget(rs.getDouble("remaining_budget"));
        advertiser.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        return advertiser;
    }
}
