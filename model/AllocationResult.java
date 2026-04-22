package model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class AllocationResult {
    private int userId;
    private int sessionId;
    private Map<AdSlot, Ad> allocatedAds;
    private Map<AdSlot, Double> scores;
    private Map<AdSlot, Double> finalPrices;
    private LocalDateTime timestamp;
    private Map<String,String> metadata; // For extensibility
     public AllocationResult() {
        this.allocatedAds = new HashMap<>();
        this.scores = new HashMap<>();
        this.finalPrices = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }
    
    public AllocationResult(int userId, int sessionId) {
        this();
        this.userId = userId;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    
    public Map<AdSlot, Ad> getAllocatedAds() { return allocatedAds; }
    public void setAllocatedAds(Map<AdSlot, Ad> allocatedAds) { this.allocatedAds = allocatedAds; }
    
    public Map<AdSlot, Double> getScores() { return scores; }
    public void setScores(Map<AdSlot, Double> scores) { this.scores = scores; }
    
    public Map<AdSlot, Double> getFinalPrices() { return finalPrices; }
    public void setFinalPrices(Map<AdSlot, Double> finalPrices) { this.finalPrices = finalPrices; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    // Utility methods
    public void addAllocatedAd(AdSlot slot, Ad ad, double score, double finalPrice) {
        allocatedAds.put(slot, ad);
        scores.put(slot, score);
        finalPrices.put(slot, finalPrice);
    }
    
    public Ad getAdForSlot(AdSlot slot) {
        return allocatedAds.get(slot);
    }
    
    public double getScoreForSlot(AdSlot slot) {
        return scores.getOrDefault(slot, 0.0);
    }
    
    public double getFinalPriceForSlot(AdSlot slot) {
        return finalPrices.getOrDefault(slot, 0.0);
    }
    
    public int getTotalAdsAllocated() {
        return allocatedAds.size();
    }
    
    public double getTotalRevenue() {
        return finalPrices.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AllocationResult{timestamp=").append(timestamp).append(", totalAds=").append(getTotalAdsAllocated())
          .append(", totalRevenue=$").append(String.format("%.2f", getTotalRevenue())).append("\n");
        
        for (Map.Entry<AdSlot, Ad> entry : allocatedAds.entrySet()) {
            AdSlot slot = entry.getKey();
            Ad ad = entry.getValue();
            double score = scores.get(slot);
            double price = finalPrices.get(slot);
            
            sb.append("  ").append(slot.getName()).append(": ").append(ad.getTitle())
              .append(" (score=").append(String.format("%.2f", score))
              .append(", price=$").append(String.format("%.2f", price)).append(")\n");
        }
        
        sb.append("}");
        return sb.toString();
    }
}
