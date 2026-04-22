package service;

import engine.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import manager.*;
import model.*;
import repository.*;

public class AdAllocationService {
    private AllocationEngine allocationEngine;
    private UserManager userManager;
    private AdvertiserManager advertiserManager;
    private SessionManager sessionManager;
    private SlotManager slotManager;
    private AdRepository adRepository;
    private AllocationRepository allocationRepository;
    private SearchHistoryRepository searchHistoryRepository;
    
    public AdAllocationService() {
        this.allocationEngine = new AllocationEngine();
        this.userManager = new UserManager();
        this.advertiserManager = new AdvertiserManager();
        this.sessionManager = new SessionManager();
        this.slotManager = new SlotManager();
        this.adRepository = new AdRepository();
        this.allocationRepository = new AllocationRepository();
        this.searchHistoryRepository = new SearchHistoryRepository();
    }
    
    public User login(String username, String email) throws SQLException {
        User user = userManager.authenticateUser(username, email);
        if (user != null) {
            // Create or get existing session
            Session session = sessionManager.getOrCreateDemoSession(user.getId());
            return user;
        }
        return null;
    }
    
    public User registerUser(String username, String email) throws SQLException {
        return userManager.createUser(username, email);
    }
    
    public AllocationResult allocateAds(String username, String searchText, String pageContent) throws SQLException {
        // Get user
        Optional<User> userOpt = userManager.getUserByUsername(username);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        
        User user = userOpt.get();
        
        // Get or create session
        Session session = sessionManager.getOrCreateDemoSession(user.getId());
        
        // Store search history
        int searchId = searchHistoryRepository.create(user.getId(), session.getId(), searchText, pageContent);
        
        // Get active ads and advertisers
        List<Ad> activeAds = adRepository.findActive();
        Map<Integer, Advertiser> activeAdvertisers = advertiserManager.getActiveAdvertiserMap();
        
        // Perform allocation
        AllocationResult result = allocationEngine.allocateAds(
            activeAds, searchText, pageContent, 
            user.getId(), session.getId(), activeAdvertisers
        );
        
        // Save allocation events
        for (Map.Entry<AdSlot, Ad> entry : result.getAllocatedAds().entrySet()) {
            AdSlot slot = entry.getKey();
            Ad ad = entry.getValue();
            double score = result.getScoreForSlot(slot);
            double price = result.getFinalPriceForSlot(slot);
            
            AllocationEvent event = new AllocationEvent(user.getId(), session.getId(), ad.getId(), slot, score, price);
            allocationRepository.create(event);
            
            // Update slot usage
            slotManager.recordAdShown(ad.getId(), slot);
            
            // Update ad performance
            adRepository.updatePerformance(ad.getId(), 1, 0); // 1 impression, 0 clicks initially
        }
        
        return result;
    }
    
    public void recordClick(String username, int adId) throws SQLException {
        // Get user
        Optional<User> userOpt = userManager.getUserByUsername(username);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        
        User user = userOpt.get();
        
        // Get session
        List<Session> sessions = sessionManager.getSessionsByUserId(user.getId());
        if (sessions.isEmpty()) {
            throw new IllegalArgumentException("No active session found for user: " + username);
        }
        
        Session session = sessions.get(0); // Get most recent session
        
        // Update allocation event
        allocationRepository.updateClickByAdAndSession(adId, session.getId());
        
        // Update ad performance
        adRepository.updatePerformance(adId, 0, 1); // 0 impressions, 1 click
        
        // Update engine
        allocationEngine.recordClick(adId, user.getId(), session.getId());
    }
    
    public List<Ad> getAllAds() throws SQLException {
        return adRepository.findAll();
    }
    
    public List<Advertiser> getAllAdvertisers() throws SQLException {
        return advertiserManager.getAllAdvertisers();
    }
    
    public List<User> getAllUsers() throws SQLException {
        return userManager.getAllUsers();
    }
    
    public List<AllocationEvent> getUserAllocationHistory(String username) throws SQLException {
        Optional<User> userOpt = userManager.getUserByUsername(username);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        
        return allocationRepository.findByUserId(userOpt.get().getId());
    }
    
    public List<AllocationEvent> getRecentAllocations(int minutesBack) throws SQLException {
        return allocationRepository.findRecent(minutesBack);
    }
    
    public double getTotalRevenue() throws SQLException {
        return allocationRepository.getTotalRevenue();
    }
    
    public Map<AdSlot, Integer> getSlotUsage() {
        return slotManager.getAllSlotUsage();
    }
    
    public Map<AdSlot, Double> getSlotPerformance() {
        return slotManager.getSlotPerformance();
    }
    
    public Advertiser createAdvertiser(String name, double budget) throws SQLException {
        return advertiserManager.createAdvertiser(name, budget);
    }
    
    public Ad createAd(int advertiserId, String title, String content, double bidAmount, String keywords) throws SQLException {
        Ad ad = new Ad(advertiserId, title, content, bidAmount, keywords);
        return adRepository.create(ad);
    }
    
    public void addAdvertiserConflict(int advertiser1Id, int advertiser2Id) throws SQLException {
        advertiserManager.addConflict(advertiser1Id, advertiser2Id);
        allocationEngine.addConflict(advertiser1Id, advertiser2Id);
    }
    
    public void removeAdvertiserConflict(int advertiser1Id, int advertiser2Id) throws SQLException {
        advertiserManager.removeConflict(advertiser1Id, advertiser2Id);
        allocationEngine.removeConflict(advertiser1Id, advertiser2Id);
    }
    
    public List<String> getPopularSearchTerms(int limit) throws SQLException {
        return searchHistoryRepository.getPopularSearchTerms(limit);
    }
    
    public void initializeSystem() throws SQLException {
        // Initialize conflicts
        advertiserManager.initializeConflicts();
        
        // Load conflicts into engine
        List<Advertiser> advertisers = advertiserManager.getAllAdvertisers();
        for (Advertiser advertiser : advertisers) {
            for (Integer conflictId : advertiser.getConflictingAdvertisers()) {
                allocationEngine.addConflict(advertiser.getId(), conflictId);
            }
        }
    }
    
    public void cleanupExpiredSessions() {
        try {
            sessionManager.cleanupExpiredSessions();
        } catch (SQLException e) {
            System.err.println("Error cleaning up expired sessions: " + e.getMessage());
        }
    }
}
