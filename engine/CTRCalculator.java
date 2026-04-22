package engine;

import model.Ad;
import model.AllocationEvent;
import dsa.slidingwindow.MemoryWindow;
import java.time.LocalDateTime;
import java.util.List;

public class CTRCalculator {
    private MemoryWindow memoryWindow;
    
    public CTRCalculator(MemoryWindow memoryWindow) {
        this.memoryWindow = memoryWindow;
    }
    
    public double calculateOverallCTR(Ad ad) {
        return ad.getCTR();
    }
    
    public double calculateRecentCTR(Ad ad, int minutesBack) {
        return memoryWindow.getAdCTR(ad.getId(), minutesBack);
    }
    
    public double calculateHourlyCTR(Ad ad) {
        return memoryWindow.getAdCTR(ad.getId(), 60);
    }
    
    public double calculateDailyCTR(Ad ad) {
        return memoryWindow.getAdCTR(ad.getId(), 60 * 24);
    }
    
    public double calculateWeightedCTR(Ad ad) {
        double overallCTR = calculateOverallCTR(ad);
        double recentCTR = calculateRecentCTR(ad, 120); // Last 2 hours
        double hourlyCTR = calculateHourlyCTR(ad);
        
        // Weight recent performance more heavily
        return (overallCTR * 0.3 + recentCTR * 0.4 + hourlyCTR * 0.3);
    }
    
    public double predictCTR(Ad ad, String context) {
        // Simple prediction based on historical performance and context relevance
        double baseCTR = calculateWeightedCTR(ad);
        
        // Adjust based on context (this would be more sophisticated in reality)
        double contextBoost = 1.0;
        if (context != null && !context.trim().isEmpty()) {
            for (String keyword : ad.getKeywords()) {
                if (context.toLowerCase().contains(keyword.toLowerCase())) {
                    contextBoost += 0.1; // 10% boost per matching keyword
                }
            }
        }
        
        return Math.min(1.0, baseCTR * contextBoost);
    }
    
    public int getClickCount(Ad ad, int minutesBack) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutesBack);
        List<AllocationEvent> events = memoryWindow.getAllEvents();
        
        int clickCount = 0;
        for (AllocationEvent event : events) {
            if (event.getAdId() == ad.getId() && 
                event.getCreatedAt().isAfter(cutoff) && 
                event.getWasClicked()) {
                clickCount++;
            }
        }
        
        return clickCount;
    }
    
    public int getShowCount(Ad ad, int minutesBack) {
        return memoryWindow.getAdShowCount(ad.getId(), minutesBack);
    }
    
    public double getClickRateBySlot(Ad ad, String slotType, int minutesBack) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutesBack);
        List<AllocationEvent> events = memoryWindow.getAllEvents();
        
        int shows = 0;
        int clicks = 0;
        
        for (AllocationEvent event : events) {
            if (event.getAdId() == ad.getId() && 
                event.getCreatedAt().isAfter(cutoff) &&
                event.getSlotType().name().equals(slotType)) {
                shows++;
                if (event.getWasClicked()) {
                    clicks++;
                }
            }
        }
        
        return shows > 0 ? (double) clicks / shows : 0.0;
    }
}
