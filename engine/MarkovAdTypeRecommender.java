package engine;

import model.UserIntentState;
import model.Ad;
import model.AdSlot;
import java.util.*;

/**
 * Recommends ad types and adjusts scoring based on predicted user intent states.
 * Maximizes ad relevance by tailoring ad selection to user's predicted next state.
 */
public class MarkovAdTypeRecommender {
    
    /**
     * Ad type categories aligned with user intent states.
     */
    public enum AdTypeCategory {
        AWARENESS("awareness", "general branding, educational content"),
        CATEGORY("category", "product categories, feature highlights"),
        COMPARISON("comparison", "price comparisons, reviews, discounts"),
        CONVERSION("conversion", "limited time offers, urgency messaging, CTAs");
        
        private final String name;
        private final String description;
        
        AdTypeCategory(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Get the recommended ad type for a given user intent state.
     * 
     * @param state User's current or predicted intent state
     * @return Recommended ad type category
     */
    public AdTypeCategory recommendAdType(UserIntentState state) {
        return switch (state) {
            case BROWSING -> AdTypeCategory.AWARENESS;
            case SEARCHING -> AdTypeCategory.CATEGORY;
            case COMPARING -> AdTypeCategory.COMPARISON;
            case BUYING -> AdTypeCategory.CONVERSION;
        };
    }
    
    /**
     * Calculate a relevance multiplier based on ad type match with user state.
     * Ads matching the predicted state get higher scores.
     * 
     * @param state User's predicted intent state
     * @param adType The ad's type (should be lowercase)
     * @return Relevance multiplier (1.0 = neutral, >1.0 = relevant, <1.0 = less relevant)
     */
    public double calculateRelevanceMultiplier(UserIntentState state, String adType) {
        AdTypeCategory recommended = recommendAdType(state);
        
        if (adType == null) {
            return 1.0;
        }
        
        String normalizedType = adType.toLowerCase().trim();
        
        // Perfect match
        if (normalizedType.equals(recommended.getName())) {
            return 1.5;
        }
        
        // Adjacent category match (can be useful)
        if (isAdjacentCategory(state, normalizedType)) {
            return 1.2;
        }
        
        // Inverse match (less useful but not harmful)
        if (normalizedType.equals("awareness")) {
            return 1.0;
        }
        
        // Default: slightly less relevant
        return 0.9;
    }
    
    /**
     * Check if ad type is adjacent/related to the current state.
     */
    private boolean isAdjacentCategory(UserIntentState state, String adType) {
        return switch (state) {
            case BROWSING -> adType.equals("category") || adType.equals("comparison");
            case SEARCHING -> adType.equals("awareness") || adType.equals("comparison");
            case COMPARING -> adType.equals("category") || adType.equals("conversion");
            case BUYING -> adType.equals("comparison") || adType.equals("awareness");
        };
    }
    
    /**
     * Get the CTR (Click-Through Rate) boost factor for a state.
     * Higher CTR boost means ads have better conversion potential.
     * 
     * @param state User's current or predicted intent state
     * @return CTR boost multiplier
     */
    public double getCTRBoostFactor(UserIntentState state) {
        return state.getCTRBoostFactor();
    }
    
    /**
     * Get bid adjustment recommendation based on predicted state.
     * Suggested bid multiplier for ads to maximize ROI at each state.
     * 
     * @param state User's predicted intent state
     * @return Bid multiplier suggestion
     */
    public double getBidAdjustmentFactor(UserIntentState state) {
        return switch (state) {
            case BROWSING -> 0.8;      // Lower bids for awareness
            case SEARCHING -> 1.0;     // Standard bids for search
            case COMPARING -> 1.3;     // Higher bids for comparisons (high intent)
            case BUYING -> 2.0;        // Maximum bids for conversion (highest intent)
        };
    }
    
    /**
     * Get the priority level for ad slots based on predicted state.
     * Determines which ad slots should be prioritized.
     * 
     * @param state User's predicted intent state
     * @return Ordered list of preferred ad slots
     */
    public List<AdSlot> getSlotPriority(UserIntentState state) {
        // Prioritize slots based on state (actual slots are TOP, SIDEBAR, FOOTER)
        return switch (state) {
            case BROWSING -> Arrays.asList(AdSlot.SIDEBAR, AdSlot.FOOTER, AdSlot.TOP);
            case SEARCHING -> Arrays.asList(AdSlot.TOP, AdSlot.SIDEBAR, AdSlot.FOOTER);
            case COMPARING -> Arrays.asList(AdSlot.TOP, AdSlot.SIDEBAR, AdSlot.FOOTER);
            case BUYING -> Arrays.asList(AdSlot.TOP, AdSlot.SIDEBAR, AdSlot.FOOTER);
        };
    }
    
    /**
     * Get recommended messaging tone for ads at this state.
     * Helps determine ad creative direction.
     * 
     * @param state User's predicted intent state
     * @return Messaging tone recommendation
     */
    public String getMessageTone(UserIntentState state) {
        return switch (state) {
            case BROWSING -> "casual, exploratory, informative";
            case SEARCHING -> "helpful, product-focused, feature-rich";
            case COMPARING -> "competitive, value-focused, detailed";
            case BUYING -> "urgent, action-oriented, benefit-focused";
        };
    }
    
    /**
     * Filter ads by relevance to the predicted state.
     * Returns only ads that match the recommended type for the state.
     * 
     * @param ads Available ads to filter
     * @param state User's predicted intent state
     * @param strict If true, only returns perfect matches; if false, includes adjacent types
     * @return Filtered list of relevant ads
     */
    public List<Ad> filterAdsByRelevance(List<Ad> ads, UserIntentState state, boolean strict) {
        if (ads == null || ads.isEmpty()) {
            return new ArrayList<>();
        }
        
        AdTypeCategory recommended = recommendAdType(state);
        List<Ad> relevant = new ArrayList<>();
        
        for (Ad ad : ads) {
            String adType = ad.getAdType() != null ? ad.getAdType().toLowerCase() : "";
            
            if (strict) {
                // Only perfect matches
                if (adType.equals(recommended.getName())) {
                    relevant.add(ad);
                }
            } else {
                // Perfect + adjacent matches
                if (adType.equals(recommended.getName()) || isAdjacentCategory(state, adType)) {
                    relevant.add(ad);
                }
            }
        }
        
        return relevant;
    }
    
    /**
     * Get a complete recommendation report for an ad at a given state.
     */
    public AdRecommendation getRecommendation(Ad ad, UserIntentState state) {
        String adType = ad.getAdType() != null ? ad.getAdType().toLowerCase() : "unknown";
        AdTypeCategory recommended = recommendAdType(state);
        
        return new AdRecommendation(
            ad.getId(),
            state,
            adType,
            recommended,
            calculateRelevanceMultiplier(state, adType),
            getCTRBoostFactor(state),
            getBidAdjustmentFactor(state)
        );
    }
    
    /**
     * Inner class for ad recommendation details.
     */
    public static class AdRecommendation {
        private final int adId;
        private final UserIntentState state;
        private final String adType;
        private final AdTypeCategory recommendedType;
        private final double relevanceMultiplier;
        private final double ctrBoostFactor;
        private final double bidAdjustmentFactor;
        
        public AdRecommendation(int adId, UserIntentState state, String adType, 
                               AdTypeCategory recommendedType, double relevanceMultiplier,
                               double ctrBoostFactor, double bidAdjustmentFactor) {
            this.adId = adId;
            this.state = state;
            this.adType = adType;
            this.recommendedType = recommendedType;
            this.relevanceMultiplier = relevanceMultiplier;
            this.ctrBoostFactor = ctrBoostFactor;
            this.bidAdjustmentFactor = bidAdjustmentFactor;
        }
        
        public int getAdId() { return adId; }
        public UserIntentState getState() { return state; }
        public String getAdType() { return adType; }
        public AdTypeCategory getRecommendedType() { return recommendedType; }
        public double getRelevanceMultiplier() { return relevanceMultiplier; }
        public double getCtrBoostFactor() { return ctrBoostFactor; }
        public double getBidAdjustmentFactor() { return bidAdjustmentFactor; }
        
        @Override
        public String toString() {
            return String.format(
                "AdRecommendation{adId=%d, state=%s, adType=%s, recommended=%s, relevance=%.2f, ctrBoost=%.2f, bidAdj=%.2f}",
                adId, state.name(), adType, recommendedType.name(), 
                relevanceMultiplier, ctrBoostFactor, bidAdjustmentFactor
            );
        }
    }
}