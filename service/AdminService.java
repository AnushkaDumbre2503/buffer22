package service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import model.*;
import repository.*;

/**
 * AdminService provides real-time analytics and reporting for administrators.
 * Fetches live data from repositories instead of using hardcoded values.
 */
public class AdminService {
    private final UserRepository userRepository;
    private final AdvertiserRepository advertiserRepository;
    private final AdRepository adRepository;
    private final AllocationRepository allocationRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    
    public AdminService() {
        this.userRepository = new UserRepository();
        this.advertiserRepository = new AdvertiserRepository();
        this.adRepository = new AdRepository();
        this.allocationRepository = new AllocationRepository();
        this.searchHistoryRepository = new SearchHistoryRepository();
    }
    
    /**
     * Get all users from database with their statistics.
     */
    public List<User> getAllUsers() throws SQLException {
        return userRepository.findAll();
    }
    
    /**
     * Get all advertisers with budget information.
     */
    public List<Advertiser> getAllAdvertisers() throws SQLException {
        return advertiserRepository.findAll();
    }
    
    /**
     * Get all active ads.
     */
    public List<Ad> getAllAds() throws SQLException {
        return adRepository.findAll();
    }
    
    /**
     * Get detected ad conflicts by analyzing keyword overlaps.
     * Uses keyword analysis to identify competing ads.
     */
    public List<AdConflict> getAdConflicts() throws SQLException {
        List<Ad> ads = adRepository.findAll();
        List<AdConflict> conflicts = new ArrayList<>();
        
        for (int i = 0; i < ads.size(); i++) {
            for (int j = i + 1; j < ads.size(); j++) {
                Ad ad1 = ads.get(i);
                Ad ad2 = ads.get(j);
                
                // Check keyword overlap
                Set<String> keywords1 = new HashSet<>(ad1.getKeywords());
                Set<String> keywords2 = new HashSet<>(ad2.getKeywords());
                
                keywords1.retainAll(keywords2);
                
                if (!keywords1.isEmpty()) {
                    AdConflict conflict = new AdConflict(
                        conflicts.size() + 1,
                        ad1.getId(),
                        ad2.getId(),
                        "Similar keywords: " + String.join(", ", keywords1)
                    );
                    conflicts.add(conflict);
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * Get slot-wise allocation with real data from allocation_events table.
     */
    public Map<AdSlot, List<AllocationRecord>> getSlotWiseAllocation() throws SQLException {
        Map<AdSlot, List<AllocationRecord>> allocation = new HashMap<>();
        
        // Initialize all slot types
        for (AdSlot slotType : AdSlot.values()) {
            allocation.put(slotType, new ArrayList<>());
        }
        
        // Get recent allocation events (last 24 hours)
        List<AllocationEvent> events = allocationRepository.getRecentEvents(24);
        
        // Group by slot type and aggregate statistics
        Map<AdSlot, Map<Integer, AllocationStats>> slotStats = new HashMap<>();
        
        for (AllocationEvent event : events) {
            AdSlot slotType = event.getSlotType();
            int adId = event.getAdId();
            
            slotStats.putIfAbsent(slotType, new HashMap<>());
            AllocationStats stats = slotStats.get(slotType)
                .computeIfAbsent(adId, k -> new AllocationStats(adId));
            
            stats.addImpression();
            if (event.getWasClicked()) {
                stats.addClick();
            }
        }
        
        // Convert to AllocationRecords
        for (Map.Entry<AdSlot, Map<Integer, AllocationStats>> entry : slotStats.entrySet()) {
            AdSlot slotType = entry.getKey();
            for (AllocationStats stats : entry.getValue().values()) {
                try {
                    Optional<Ad> ad = adRepository.findById(stats.adId);
                    if (ad.isPresent()) {
                        AllocationRecord record = new AllocationRecord(
                            ad.get().getTitle(),
                            stats.impressions,
                            stats.clicks
                        );
                        allocation.get(slotType).add(record);
                    }
                } catch (SQLException e) {
                    // Log and continue
                }
            }
        }
        
        return allocation;
    }
    
    /**
     * Get recent system activities from various event logs.
     */
    public List<ActivityLog> getRecentActivities(int hoursBack) throws SQLException {
        List<ActivityLog> activities = new ArrayList<>();
        
        // Get allocation events
        List<AllocationEvent> allocationEvents = allocationRepository.getRecentEvents(hoursBack);
        for (AllocationEvent event : allocationEvents) {
            String details = String.format("Ad ID: %d, Slot: %s", event.getAdId(), event.getSlotType());
            ActivityLog log = new ActivityLog(
                event.getCreatedAt(),
                "Ad Allocation",
                "User " + event.getUserId(),
                details
            );
            activities.add(log);
        }
        
        // Get search history
        List<SearchHistoryRepository.SearchHistoryEntry> searches = searchHistoryRepository.getRecentSearches(hoursBack);
        for (SearchHistoryRepository.SearchHistoryEntry entry : searches) {
            ActivityLog log = new ActivityLog(
                entry.getCreatedAt(),
                "Search",
                "User " + entry.getUserId(),
                "Query: " + entry.getSearchQuery()
            );
            activities.add(log);
        }
        
        // Sort by timestamp (most recent first)
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        return activities;
    }
    
    /**
     * Get popular search terms with frequency and trends.
     */
    public List<SearchHistoryRepository.PopularSearchTerm> getPopularSearchTerms(int limit) throws SQLException {
        return searchHistoryRepository.getPopularSearchTermsWithStats(limit);
    }
    
    /**
     * Get comprehensive system statistics in real-time.
     */
    public SystemStatistics getSystemStatistics() throws SQLException {
        SystemStatistics stats = new SystemStatistics();
        
        // Count totals
        stats.setTotalUsers(userRepository.count());
        stats.setTotalAdvertisers(advertiserRepository.count());
        stats.setTotalAds(adRepository.count());
        
        // Get conflicts
        stats.setTotalConflicts(getAdConflicts().size());
        
        // Get allocation metrics (last 30 days)
        List<AllocationEvent> allocationEvents = allocationRepository.getRecentEvents(30 * 24);
        stats.setTotalImpressions(allocationEvents.size());
        stats.setTotalClicks((int) allocationEvents.stream().filter(AllocationEvent::getWasClicked).count());
        
        if (stats.getTotalImpressions() > 0) {
            stats.setOverallCTR((double) stats.getTotalClicks() / stats.getTotalImpressions() * 100);
        }
        
        // Calculate total revenue
        double totalRevenue = 0;
        for (AllocationEvent event : allocationEvents) {
            totalRevenue += event.getFinalPrice();
        }
        stats.setTotalRevenue(totalRevenue);
        
        return stats;
    }
    
    /**
     * Helper class for storing allocation statistics.
     */
    private static class AllocationStats {
        int adId;
        int impressions = 0;
        int clicks = 0;
        
        AllocationStats(int adId) {
            this.adId = adId;
        }
        
        void addImpression() {
            impressions++;
        }
        
        void addClick() {
            clicks++;
        }
    }
    
    /**
     * Data class for slot-wise allocation display.
     */
    public static class AllocationRecord {
        public String adTitle;
        public int impressions;
        public int clicks;
        
        public AllocationRecord(String adTitle, int impressions, int clicks) {
            this.adTitle = adTitle;
            this.impressions = impressions;
            this.clicks = clicks;
        }
    }
    
    /**
     * Data class for activity logs.
     */
    public static class ActivityLog {
        public LocalDateTime timestamp;
        public String type;
        public String user;
        public String details;
        
        public ActivityLog(LocalDateTime timestamp, String type, String user, String details) {
            this.timestamp = timestamp;
            this.type = type;
            this.user = user;
            this.details = details;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Data class for search terms statistics.
     */
    public static class SearchTerm {
        public String term;
        public int count;
        public LocalDateTime lastSearched;
        
        public SearchTerm(String term, int count, LocalDateTime lastSearched) {
            this.term = term;
            this.count = count;
            this.lastSearched = lastSearched;
        }
    }
    /**
     * Data class for system statistics.
     */
    public static class SystemStatistics {
        private int totalUsers;
        private int totalAdvertisers;
        private int totalAds;
        private int totalConflicts;
        private int totalImpressions;
        private int totalClicks;
        private double overallCTR;
        private double totalRevenue;
        
        public int getTotalUsers() {
            return totalUsers;
        }
        
        public void setTotalUsers(int totalUsers) {
            this.totalUsers = totalUsers;
        }
        
        public int getTotalAdvertisers() {
            return totalAdvertisers;
        }
        
        public void setTotalAdvertisers(int totalAdvertisers) {
            this.totalAdvertisers = totalAdvertisers;
        }
        
        public int getTotalAds() {
            return totalAds;
        }
        
        public void setTotalAds(int totalAds) {
            this.totalAds = totalAds;
        }
        
        public int getTotalConflicts() {
            return totalConflicts;
        }
        
        public void setTotalConflicts(int totalConflicts) {
            this.totalConflicts = totalConflicts;
        }
        
        public int getTotalImpressions() {
            return totalImpressions;
        }
        
        public void setTotalImpressions(int totalImpressions) {
            this.totalImpressions = totalImpressions;
        }
        
        public int getTotalClicks() {
            return totalClicks;
        }
        
        public void setTotalClicks(int totalClicks) {
            this.totalClicks = totalClicks;
        }
        
        public double getOverallCTR() {
            return overallCTR;
        }
        
        public void setOverallCTR(double overallCTR) {
            this.overallCTR = overallCTR;
        }
        
        public double getTotalRevenue() {
            return totalRevenue;
        }
        
        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
    }
}
