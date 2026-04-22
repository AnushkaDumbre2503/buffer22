package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SearchHistoryRepository {
    
    public int create(int userId, int sessionId, String searchQuery, String pageContent) throws SQLException {
        String sql = "INSERT INTO search_history (user_id, session_id, search_query, page_content) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (userId > 0) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            if (sessionId > 0) {
                stmt.setInt(2, sessionId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.setString(3, searchQuery);
            stmt.setString(4, pageContent);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating search history failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating search history failed, no ID obtained.");
                }
            }
        }
    }
    
    public Optional<SearchHistoryEntry> findById(int id) throws SQLException {
        String sql = "SELECT * FROM search_history WHERE id = ?";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSearchHistoryEntry(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<SearchHistoryEntry> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM search_history WHERE user_id = ? ORDER BY created_at DESC";
        List<SearchHistoryEntry> entries = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToSearchHistoryEntry(rs));
                }
            }
        }
        
        return entries;
    }
    
    public List<SearchHistoryEntry> findBySessionId(int sessionId) throws SQLException {
        String sql = "SELECT * FROM search_history WHERE session_id = ? ORDER BY created_at DESC";
        List<SearchHistoryEntry> entries = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToSearchHistoryEntry(rs));
                }
            }
        }
        
        return entries;
    }
    
    public List<SearchHistoryEntry> findRecent(int minutesBack) throws SQLException {
        String sql = "SELECT * FROM search_history WHERE created_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE) ORDER BY created_at DESC";
        List<SearchHistoryEntry> entries = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, minutesBack);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToSearchHistoryEntry(rs));
                }
            }
        }
        
        return entries;
    }
    
    public List<String> getPopularSearchTerms(int limit) throws SQLException {
        String sql = "SELECT search_query, COUNT(*) as frequency " +
                    "FROM search_history " +
                    "WHERE search_query IS NOT NULL AND search_query != '' " +
                    "GROUP BY search_query " +
                    "ORDER BY frequency DESC " +
                    "LIMIT ?";
        
        List<String> popularTerms = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    popularTerms.add(rs.getString("search_query"));
                }
            }
        }
        
        return popularTerms;
    }
    
    public List<String> getUserSearchHistory(int userId, int limit) throws SQLException {
        String sql = "SELECT DISTINCT search_query " +
                    "FROM search_history " +
                    "WHERE user_id = ? AND search_query IS NOT NULL AND search_query != '' " +
                    "ORDER BY created_at DESC " +
                    "LIMIT ?";
        
        List<String> searchHistory = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    searchHistory.add(rs.getString("search_query"));
                }
            }
        }
        
        return searchHistory;
    }
    
    public int getTotalSearches() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM search_history WHERE search_query IS NOT NULL AND search_query != ''";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        
        return 0;
    }
    
    public int getUniqueSearchers() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT user_id) as count FROM search_history WHERE user_id IS NOT NULL";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        
        return 0;
    }
    
    public boolean deleteOldEntries(int daysOld) throws SQLException {
        String sql = "DELETE FROM search_history WHERE created_at < DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, daysOld);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public List<SearchHistoryEntry> findByTimeRange(java.time.LocalDateTime startTime, 
                                                    java.time.LocalDateTime endTime) throws SQLException {
        String sql = "SELECT * FROM search_history WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        List<SearchHistoryEntry> entries = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToSearchHistoryEntry(rs));
                }
            }
        }
        
        return entries;
    }
    
    public List<SearchHistoryEntry> getRecentSearches(int hoursBack) throws SQLException {
        String sql = "SELECT * FROM search_history WHERE created_at >= DATE_SUB(NOW(), INTERVAL ? HOUR) ORDER BY created_at DESC";
        List<SearchHistoryEntry> entries = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, hoursBack);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(mapResultSetToSearchHistoryEntry(rs));
                }
            }
        }
        
        return entries;
    }
    
    public List<PopularSearchTerm> getPopularSearchTermsWithStats(int limit) throws SQLException {
        String sql = "SELECT search_query, COUNT(*) as frequency, MAX(created_at) as last_searched " +
                    "FROM search_history " +
                    "WHERE search_query IS NOT NULL AND search_query != '' " +
                    "GROUP BY search_query " +
                    "ORDER BY frequency DESC " +
                    "LIMIT ?";
        
        List<PopularSearchTerm> popularTerms = new ArrayList<>();
        
        try (Connection conn = database.DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PopularSearchTerm term = new PopularSearchTerm(
                        rs.getString("search_query"),
                        rs.getInt("frequency"),
                        rs.getTimestamp("last_searched").toLocalDateTime()
                    );
                    popularTerms.add(term);
                }
            }
        }
        
        return popularTerms;
    }
    
    private SearchHistoryEntry mapResultSetToSearchHistoryEntry(ResultSet rs) throws SQLException {
        SearchHistoryEntry entry = new SearchHistoryEntry();
        entry.setId(rs.getInt("id"));
        entry.setUserId(rs.getInt("user_id"));
        entry.setSessionId(rs.getInt("session_id"));
        entry.setSearchQuery(rs.getString("search_query"));
        entry.setPageContent(rs.getString("page_content"));
        entry.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        return entry;
    }
    
    // Inner class to represent search history entries
    public static class SearchHistoryEntry {
        private int id;
        private int userId;
        private int sessionId;
        private String searchQuery;
        private String pageContent;
        private java.time.LocalDateTime createdAt;
        
        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        
        public int getSessionId() { return sessionId; }
        public void setSessionId(int sessionId) { this.sessionId = sessionId; }
        
        public String getSearchQuery() { return searchQuery; }
        public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
        
        public String getPageContent() { return pageContent; }
        public void setPageContent(String pageContent) { this.pageContent = pageContent; }
        
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        @Override
        public String toString() {
            return String.format("SearchHistory{id=%d, userId=%d, query='%s', time=%s}", 
                               id, userId, searchQuery, createdAt);
        }
    }
    
    // Inner class to represent popular search terms with statistics
    public static class PopularSearchTerm {
        private String term;
        private int count;
        private java.time.LocalDateTime lastSearched;
        
        public PopularSearchTerm(String term, int count, java.time.LocalDateTime lastSearched) {
            this.term = term;
            this.count = count;
            this.lastSearched = lastSearched;
        }
        
        public String getTerm() { return term; }
        public int getCount() { return count; }
        public java.time.LocalDateTime getLastSearched() { return lastSearched; }
        
        @Override
        public String toString() {
            return String.format("PopularSearchTerm{term='%s', count=%d, lastSearched=%s}", 
                               term, count, lastSearched);
        }
    }
}

