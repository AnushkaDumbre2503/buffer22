package engine;

import model.Ad;
import model.AdSlot;
import dsa.slidingwindow.MemoryWindow;
import java.util.Map;

public class ScoringEngine {
    private MemoryWindow memoryWindow;
    private static final double BASE_SCORE = 1.0;
    private static final double MIN_SCORE = 0.01;
    
    public ScoringEngine(MemoryWindow memoryWindow) {
        this.memoryWindow = memoryWindow;
    }
    
    public double calculateScore(Ad ad, AdSlot slot, double contextMatch, int userId) {
        if (ad == null || slot == null) return MIN_SCORE;
        
        // Check if ad is in cooldown
        if (memoryWindow.isAdInCooldown(ad.getId(), slot.getCooldownSeconds())) {
            return MIN_SCORE;
        }
        
        // Check if advertiser has budget
        if (ad.getBidAmount() <= 0) {
            return MIN_SCORE;
        }
        
        // Core scoring formula: Score = bid × CTR × slotWeight × contextMatch × memoryBoost × fatigueControl
        double bid = ad.getBidAmount();
        double ctr = calculateEffectiveCTR(ad);
        double slotWeight = slot.getWeight();
        double memoryBoost = memoryWindow.getMemoryBoost(ad.getId(), 60); // Last hour
        double fatigueControl = memoryWindow.getFatigueScore(ad.getId(), 30); // Last 30 minutes
        
        double score = bid * ctr * slotWeight * contextMatch * memoryBoost * fatigueControl;
        
        return Math.max(MIN_SCORE, score);
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
