package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.Ad;

public class AdRepository {
    
    public Ad create(Ad ad) throws SQLException {
        String sql = "INSERT INTO ads (advertiser_id, title, content, bid_amount, keywords) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, ad.getAdvertiserId());
            stmt.setString(2, ad.getTitle());
            stmt.setString(3, ad.getContent());
            stmt.setDouble(4, ad.getBidAmount());
            stmt.setString(5, String.join(",", ad.getKeywords()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating ad failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ad.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating ad failed, no ID obtained.");
                }
            }
            
            return ad;
        }
    }
    
    public Optional<Ad> findById(int id) throws SQLException {
        String sql = "SELECT * FROM ads WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAd(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Ad> findAll() throws SQLException {
        String sql = "SELECT * FROM ads ORDER BY id DESC";
        List<Ad> ads = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                ads.add(mapResultSetToAd(rs));
            }
        }
        
        return ads;
    }
    
    public List<Ad> findByAdvertiserId(int advertiserId) throws SQLException {
        String sql = "SELECT * FROM ads WHERE advertiser_id = ? ORDER BY id DESC";
        List<Ad> ads = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, advertiserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ads.add(mapResultSetToAd(rs));
                }
            }
        }
        
        return ads;
    }
    
    public List<Ad> findActive() throws SQLException {
        String sql = "SELECT a.* FROM ads a " +
                    "JOIN advertisers adv ON a.advertiser_id = adv.id " +
                    "WHERE adv.remaining_budget >= a.bid_amount " +
                    "ORDER BY a.bid_amount DESC";
        List<Ad> ads = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                ads.add(mapResultSetToAd(rs));
            }
        }
        
        return ads;
    }
    
    public List<Ad> findByKeyword(String keyword) throws SQLException {
        String sql = "SELECT * FROM ads WHERE keywords LIKE ? ORDER BY bid_amount DESC";
        List<Ad> ads = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + keyword + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ads.add(mapResultSetToAd(rs));
                }
            }
        }
        
        return ads;
    }
    
    public boolean update(Ad ad) throws SQLException {
        String sql = "UPDATE ads SET title = ?, content = ?, bid_amount = ?, keywords = ? WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, ad.getTitle());
            stmt.setString(2, ad.getContent());
            stmt.setDouble(3, ad.getBidAmount());
            stmt.setString(4, String.join(",", ad.getKeywords()));
            stmt.setInt(5, ad.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateBid(int adId, double newBidAmount) throws SQLException {
        String sql = "UPDATE ads SET bid_amount = ? WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, newBidAmount);
            stmt.setInt(2, adId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM ads WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public void updatePerformance(int adId, int totalShown, int totalClicked) throws SQLException {
        String sql = "INSERT INTO ad_performance (ad_id, total_shown, total_clicked) " +
                    "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " +
                    "total_shown = total_shown + VALUES(total_shown), " +
                    "total_clicked = total_clicked + VALUES(total_clicked), " +
                    "last_shown = IF(VALUES(total_shown) > 0, NOW(), last_shown), " +
                    "last_clicked = IF(VALUES(total_clicked) > 0, NOW(), last_clicked)";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adId);
            stmt.setInt(2, totalShown);
            stmt.setInt(3, totalClicked);
            stmt.executeUpdate();
        }
    }
    
    public void loadPerformance(Ad ad) throws SQLException {
        String sql = "SELECT * FROM ad_performance WHERE ad_id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ad.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ad.setTotalShown(rs.getInt("total_shown"));
                    ad.setTotalClicked(rs.getInt("total_clicked"));
                    
                    Timestamp lastShown = rs.getTimestamp("last_shown");
                    if (lastShown != null) {
                        ad.setLastShown(lastShown.toLocalDateTime());
                    }
                    
                    Timestamp lastClicked = rs.getTimestamp("last_clicked");
                    if (lastClicked != null) {
                        ad.setLastClicked(lastClicked.toLocalDateTime());
                    }
                }
            }
        }
    }
    
    public List<Ad> getTopPerformingAds(int limit) throws SQLException {
        String sql = "SELECT a.*, ap.total_shown, ap.total_clicked FROM ads a " +
                    "LEFT JOIN ad_performance ap ON a.id = ap.ad_id " +
                    "ORDER BY (CASE WHEN ap.total_shown > 0 THEN ap.total_clicked / ap.total_shown ELSE 0 END) DESC " +
                    "LIMIT ?";
        List<Ad> ads = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ad ad = mapResultSetToAd(rs);
                    ad.setTotalShown(rs.getInt("total_shown"));
                    ad.setTotalClicked(rs.getInt("total_clicked"));
                    ads.add(ad);
                }
            }
        }
        
        return ads;
    }
    
    public double getAverageBid() throws SQLException {
        String sql = "SELECT AVG(bid_amount) as avg_bid FROM ads";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("avg_bid");
            }
        }
        
        return 0.0;
    }
    
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM ads";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        
        return 0;
    }
    
    private Ad mapResultSetToAd(ResultSet rs) throws SQLException {
        Ad ad = new Ad();
        ad.setId(rs.getInt("id"));
        ad.setAdvertiserId(rs.getInt("advertiser_id"));
        ad.setTitle(rs.getString("title"));
        ad.setContent(rs.getString("content"));
        ad.setBidAmount(rs.getDouble("bid_amount"));
        
        String keywordsStr = rs.getString("keywords");
        if (keywordsStr != null && !keywordsStr.trim().isEmpty()) {
            ad.setKeywords(java.util.Arrays.asList(keywordsStr.split(",")));
        }
        
        ad.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        return ad;
    }
}
