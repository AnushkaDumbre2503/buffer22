package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ad {
    private int id;
    private int advertiserId;
    private String title;
    private String content;
    private double bidAmount;
    private List<String> keywords;
    private String adType; // awareness, category, comparison, conversion
    private LocalDateTime createdAt;
    
    // Performance metrics
    private int totalShown;
    private int totalClicked;
    private LocalDateTime lastShown;
    private LocalDateTime lastClicked;
    
    public Ad() {}
    
    public Ad(int id, int advertiserId, String title, String content, double bidAmount, String keywords) {
        this.id = id;
        this.advertiserId = advertiserId;
        this.title = title;
        this.content = content;
        this.bidAmount = bidAmount;
        this.keywords = keywords != null ? Arrays.asList(keywords.split(",")) : new ArrayList<>();
        this.adType = "awareness"; // default type
        this.createdAt = LocalDateTime.now();
        this.totalShown = 0;
        this.totalClicked = 0;
    }
    
    public Ad(int advertiserId, String title, String content, double bidAmount, String keywords) {
        this.advertiserId = advertiserId;
        this.title = title;
        this.content = content;
        this.bidAmount = bidAmount;
        this.keywords = keywords != null ? Arrays.asList(keywords.split(",")) : new ArrayList<>();
        this.adType = "awareness"; // default type
        this.createdAt = LocalDateTime.now();
        this.totalShown = 0;
        this.totalClicked = 0;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getAdvertiserId() { return advertiserId; }
    public void setAdvertiserId(int advertiserId) { this.advertiserId = advertiserId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }
    
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    
    public String getAdType() { return adType; }
    public void setAdType(String adType) { this.adType = adType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public int getTotalShown() { return totalShown; }
    public void setTotalShown(int totalShown) { this.totalShown = totalShown; }
    
    public int getTotalClicked() { return totalClicked; }
    public void setTotalClicked(int totalClicked) { this.totalClicked = totalClicked; }
    
    public LocalDateTime getLastShown() { return lastShown; }
    public void setLastShown(LocalDateTime lastShown) { this.lastShown = lastShown; }
    
    public LocalDateTime getLastClicked() { return lastClicked; }
    public void setLastClicked(LocalDateTime lastClicked) { this.lastClicked = lastClicked; }
    
    // Performance methods
    public double getCTR() {
        return totalShown > 0 ? (double) totalClicked / totalShown : 0.0;
    }
    
    public void incrementShown() {
        this.totalShown++;
        this.lastShown = LocalDateTime.now();
    }
    
    public void incrementClicked() {
        this.totalClicked++;
        this.lastClicked = LocalDateTime.now();
    }
    
    public boolean containsKeyword(String keyword) {
        return keywords.stream().anyMatch(k -> k.equalsIgnoreCase(keyword.trim()));
    }
    
    @Override
    public String toString() {
        return "Ad{id=" + id + ", title='" + title + "', bid=" + bidAmount + ", CTR=" + String.format("%.2f%%", getCTR() * 100) + "}";
    }
}
