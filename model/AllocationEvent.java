package model;

import java.time.LocalDateTime;

public class AllocationEvent {
    private int id;
    private int userId;
    private int sessionId;
    private int adId;
    private AdSlot slotType;
    private double score;
    private double finalPrice;
    private boolean wasClicked;
    private LocalDateTime createdAt;
    
    public AllocationEvent() {}
    
    public AllocationEvent(int userId, int sessionId, int adId, AdSlot slotType, double score, double finalPrice) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.adId = adId;
        this.slotType = slotType;
        this.score = score;
        this.finalPrice = finalPrice;
        this.wasClicked = false;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    
    public int getAdId() { return adId; }
    public void setAdId(int adId) { this.adId = adId; }
    
    public AdSlot getSlotType() { return slotType; }
    public void setSlotType(AdSlot slotType) { this.slotType = slotType; }
    
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    
    public double getFinalPrice() { return finalPrice; }
    public void setFinalPrice(double finalPrice) { this.finalPrice = finalPrice; }
    
    public boolean getWasClicked() { return wasClicked; }
    public void setWasClicked(boolean wasClicked) { this.wasClicked = wasClicked; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Utility methods
    public void markAsClicked() {
        this.wasClicked = true;
    }
    
    @Override
    public String toString() {
        return "AllocationEvent{id=" + id + ", adId=" + adId + ", slot=" + slotType.getName() 
             + ", score=" + String.format("%.2f", score) + ", price=$" + String.format("%.2f", finalPrice)
             + ", clicked=" + wasClicked + ", time=" + createdAt + "}";
    }
}
