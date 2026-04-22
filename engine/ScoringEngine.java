package engine;

import dsa.slidingwindow.MemoryWindow;
import model.Ad;
import model.AdSlot;

public class ScoringEngine {
    private MemoryWindow memoryWindow;
    private static final double BASE_SCORE = 1.0;
    private static final double MIN_SCORE = 0.001;  // Lower minimum to not filter out ads
    
    public ScoringEngine(MemoryWindow memoryWindow) {
        this.memoryWindow = memoryWindow;
    }
    
    public double calculateScore(Ad ad, AdSlot slot, double contextMatch, int userId) {
        if (ad == null || slot == null) return MIN_SCORE;
        
        // Check if ad is in cooldown
        if (memoryWindow.isAdInCooldown(ad.getId(), slot.getCooldownSeconds())) {
            return MIN_SCORE;
        }
        
        // Check if ad has positive bid
        if (ad.getBidAmount() <= 0) {
            return MIN_SCORE;
        }
        
        // Ensure minimum context match score
        if (contextMatch < 0.1) {
            contextMatch = 0.1;
        }
        
        // IMPROVED SCORING FORMULA - Optimized for relevance + bid
        // Key insight: relevance (context match) should dominate the score
        // Score = contextMatch × bid × slotWeight × ctr × memoryBoost × fatigueControl
        
        double contextBoost = contextMatch;  // 0.3 to 3.0 from ContextMatcher
        double bid = Math.max(1.0, ad.getBidAmount() / 10.0);  // Normalize bid to 0.1-5.0 range
        double ctr = calculateEffectiveCTR(ad);  // 0.01 to 0.5+
        double slotWeight = slot.getWeight();  // 0.7 to 1.5
        double memoryBoost = Math.max(0.8, memoryWindow.getMemoryBoost(ad.getId(), 60));  // Min 0.8x
        double fatigueControl = Math.max(0.5, memoryWindow.getFatigueScore(ad.getId(), 30));  // Min 0.5
        
        // Priority: context > bid > ctr > slot > memory > fatigue
        double score = contextBoost * bid * ctr * slotWeight * memoryBoost * fatigueControl;
        
        // Minimum base score for any valid ad
        return Math.max(0.01, score);  // Ensure all valid ads have at least 0.01
    }
    
    private double calculateEffectiveCTR(Ad ad) {
        double historicalCTR = ad.getCTR();
        double recentCTR = memoryWindow.getAdCTR(ad.getId(), 120); // Last 2 hours
        
        // Weight recent performance more heavily
        double effectiveCTR = (historicalCTR * 0.3 + recentCTR * 0.7);
        
        // Ensure minimum CTR to avoid zero scores
        return Math.max(0.01, effectiveCTR);
    }
    
    public double calculateSecondPrice(double winnerScore, double secondHighestScore) {
        // Second-price auction: winner pays second highest bid's score
        // Convert score back to price approximation
        return Math.max(0.01, secondHighestScore * 0.8); // 80% of second highest as payment
    }
    
    public boolean isAdEligible(Ad ad, AdSlot slot) {
        // Check basic eligibility
        if (ad == null || slot == null) return false;
        if (ad.getBidAmount() <= 0) return false;
        
        // Check cooldown
        if (memoryWindow.isAdInCooldown(ad.getId(), slot.getCooldownSeconds())) {
            return false;
        }
        
        // Check fatigue threshold
        double fatigueScore = memoryWindow.getFatigueScore(ad.getId(), 30);
        if (fatigueScore < 0.2) { // Too much fatigue
            return false;
        }
        
        return true;
    }
    
    public void updateAdPerformance(Ad ad, boolean wasClicked) {
        if (wasClicked) {
            ad.incrementClicked();
        }
        ad.incrementShown();
    }
    
    public double getAdQualityScore(Ad ad) {
        double ctr = ad.getCTR();
        double recentCTR = memoryWindow.getAdCTR(ad.getId(), 60);
        double fatigueScore = memoryWindow.getFatigueScore(ad.getId(), 30);
        double memoryBoost = memoryWindow.getMemoryBoost(ad.getId(), 60);
        
        // Quality score combines multiple factors
        return (ctr * 0.4 + recentCTR * 0.4 + fatigueScore * 0.1 + memoryBoost * 0.1);
    }
}
