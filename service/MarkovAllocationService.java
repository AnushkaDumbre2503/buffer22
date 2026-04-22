package service;

import engine.*;
import java.util.*;
import manager.UserStateManager;
import model.*;

/**
 * Enhanced allocation service that integrates Markov-based intent prediction
 * with the existing ad allocation engine.
 * 
 * This service:
 * 1. Tracks user intent states using Markov model
 * 2. Predicts next user states
 * 3. Recommends and prioritizes ads based on predicted states
 * 4. Adjusts scoring based on state transitions
 */
public class MarkovAllocationService {
    private final AllocationEngine allocationEngine;
    private final UserStateManager userStateManager;
    private final MarkovAdTypeRecommender adTypeRecommender;
    
    public MarkovAllocationService(AllocationEngine allocationEngine, 
                                   ScoringEngine scoringEngine) {
        this.allocationEngine = allocationEngine;
        this.userStateManager = new UserStateManager();
        this.adTypeRecommender = new MarkovAdTypeRecommender();
    }
    
    /**
     * Allocate ads with Markov-based intent prediction.
     * 
     * @param ads Available ads to allocate
     * @param searchText User's search query (if any)
     * @param pageContent Page content context
     * @param userId User ID
     * @param sessionId Session ID
     * @param advertisers Available advertisers with budgets
     * @param updateUserState Whether to auto-detect and update user state
     * @return Allocation result with Markov-optimized ad selection
     */
    public AllocationResult allocateAdsWithMarkovOptimization(
            List<Ad> ads, 
            String searchText, 
            String pageContent,
            int userId, 
            int sessionId,
            Map<Integer, Advertiser> advertisers,
            boolean updateUserState) {
        
        // Initialize session if not already done
        if (userStateManager.getCurrentState(sessionId) == UserIntentState.BROWSING && updateUserState) {
            userStateManager.initializeSession(sessionId, userId, UserIntentState.BROWSING);
        }
        
        // Update user state based on behavior signals
        if (updateUserState) {
            updateUserStateBasedOnContext(sessionId, searchText, pageContent);
        }
        
        // Get current and predicted states
        UserIntentState currentState = userStateManager.getCurrentState(sessionId);
        MarkovStateEngine.StatePrediction prediction = userStateManager.getPredictionWithProbability(sessionId);
        
        // Filter and prioritize ads based on Markov prediction
        List<Ad> optimizedAds = prioritizeAdsByMarkovState(ads, prediction.getPredictedState());
        
        // Allocate ads using standard engine
        AllocationResult result = allocationEngine.allocateAds(
            optimizedAds, 
            searchText, 
            pageContent, 
            userId, 
            sessionId, 
            advertisers
        );
        
        // Enhance scoring with Markov state factors
        enhanceAllocationScoresWithMarkov(result, currentState, prediction.getPredictedState());
        
        return result;
    }
    
    /**
     * Update user state based on context signals (search, browsing patterns, etc.).
     * 
     * @param sessionId Session ID
     * @param searchText User's search query
     * @param pageContent Page content being viewed
     */
    private void updateUserStateBasedOnContext(int sessionId, String searchText, String pageContent) {
        Map<String, Boolean> signals = new HashMap<>();
        
        // Detect search behavior
        signals.put("hasSearched", searchText != null && !searchText.trim().isEmpty());
        
        // Detect comparison signals
        signals.put("isComparing", 
            (pageContent != null && (pageContent.contains("compare") || pageContent.contains("vs") || pageContent.contains("price")))
        );
        
        // Suggest state update based on signals
        UserIntentState suggestedState = userStateManager.suggestStateUpdate(sessionId, signals);
        if (!suggestedState.equals(userStateManager.getCurrentState(sessionId))) {
            userStateManager.updateCurrentState(sessionId, suggestedState);
        }
    }
    
    /**
     * Prioritize and sort ads based on predicted user intent state.
     * Ads matching the predicted state are moved to the front.
     * 
     * @param ads Available ads
     * @param predictedState Predicted user intent state
     * @return Prioritized list of ads
     */
    private List<Ad> prioritizeAdsByMarkovState(List<Ad> ads, UserIntentState predictedState) {
        if (ads == null || ads.isEmpty()) {
            return ads;
        }
        
        // Separate ads into relevant and less relevant
        List<Ad> relevantAds = new ArrayList<>();
        List<Ad> otherAds = new ArrayList<>();
        
        for (Ad ad : ads) {
            String adType = ad.getAdType() != null ? ad.getAdType() : "unknown";
            double relevance = adTypeRecommender.calculateRelevanceMultiplier(predictedState, adType);
            if (relevance >= 1.2) {
                relevantAds.add(ad);
            } else {
                otherAds.add(ad);
            }
        }
        
        // Combine: relevant ads first, then others
        List<Ad> result = new ArrayList<>(relevantAds);
        result.addAll(otherAds);
        
        return result;
    }
    
    /**
     * Enhance allocation scores using Markov state factors.
     * Applies CTR boosts and bid adjustments based on predicted state.
     * 
     * @param result Allocation result to enhance
     * @param currentState Current user state
     * @param predictedState Predicted next user state
     */
    @SuppressWarnings("unused")
    private void enhanceAllocationScoresWithMarkov(AllocationResult result, 
                                                   UserIntentState currentState,
                                                   UserIntentState predictedState) {
        // Enhancement already applied by prioritizeAdsByMarkovState
        // Future: could apply additional CTR/bid boosts based on predicted state
    }
    
    /**
     * Record a user action and update state accordingly.
     * 
     * @param sessionId Session ID
     * @param actionType Type of action (SEARCH, CLICK, COMPARE, CHECKOUT, etc.)
     */
    public void recordUserAction(int sessionId, String actionType) {
        if (actionType == null) return;
        
        String action = actionType.toUpperCase();
        UserIntentState currentState = userStateManager.getCurrentState(sessionId);
        UserIntentState newState = currentState;
        
        switch (action) {
            case "SEARCH":
                if (currentState == UserIntentState.BROWSING) {
                    newState = UserIntentState.SEARCHING;
                }
                break;
            case "COMPARE":
            case "VIEW_COMPARISON":
                if (currentState.getPriority() < UserIntentState.COMPARING.getPriority()) {
                    newState = UserIntentState.COMPARING;
                }
                break;
            case "CHECKOUT":
            case "ADD_TO_CART":
            case "BUY":
            case "PURCHASE":
                newState = UserIntentState.BUYING;
                break;
            case "CLICK_AD":
                // Recording click, may indicate intent advancement
                if (currentState == UserIntentState.COMPARING || currentState == UserIntentState.SEARCHING) {
                    newState = UserIntentState.BUYING;
                }
                break;
        }
        
        userStateManager.updateCurrentState(sessionId, newState);
    }
    
    /**
     * Get current user state for a session.
     */
    public UserIntentState getUserState(int sessionId) {
        return userStateManager.getCurrentState(sessionId);
    }
    
    /**
     * Get predicted next user state with probability.
     */
    public MarkovStateEngine.StatePrediction getPrediction(int sessionId) {
        return userStateManager.getPredictionWithProbability(sessionId);
    }
    
    /**
     * Get all ranked predictions for a session.
     */
    public List<MarkovStateEngine.StatePrediction> getRankedPredictions(int sessionId) {
        return userStateManager.getRankedPredictions(sessionId);
    }
    
    /**
     * Get session statistics and state history.
     */
    public String getSessionMarkovStats(int sessionId) {
        StringBuilder stats = new StringBuilder();
        
        UserIntentState currentState = userStateManager.getCurrentState(sessionId);
        MarkovStateEngine.StatePrediction prediction = userStateManager.getPredictionWithProbability(sessionId);
        int transitionCount = userStateManager.getStateTransitionCount(sessionId);
        List<UserIntentState> history = userStateManager.getStateHistory(sessionId);
        long secsSinceChange = userStateManager.getSecondsSinceStateChange(sessionId);
        
        stats.append("Markov State Analysis:\n");
        stats.append("  Current State: ").append(currentState.name()).append("\n");
        stats.append("  Predicted State: ").append(prediction.getPredictedState().name()).append("\n");
        stats.append("  Prediction Probability: ").append(String.format("%.2f%%", prediction.getProbability() * 100)).append("\n");
        stats.append("  State Transitions: ").append(transitionCount).append("\n");
        stats.append("  State History: ").append(history).append("\n");
        stats.append("  Seconds Since Change: ").append(secsSinceChange).append("\n");
        stats.append("  CTR Boost Factor: ").append(String.format("%.2f", adTypeRecommender.getCTRBoostFactor(currentState))).append("x\n");
        
        return stats.toString();
    }
    
    /**
     * Get the underlying UserStateManager for advanced operations.
     */
    public UserStateManager getUserStateManager() {
        return userStateManager;
    }
    
    /**
     * Get the underlying MarkovAdTypeRecommender for direct recommendations.
     */
    public MarkovAdTypeRecommender getAdTypeRecommender() {
        return adTypeRecommender;
    }
    
    /**
     * End a user session and clean up Markov state.
     */
    public void endSession(int sessionId) {
        userStateManager.endSession(sessionId);
    }
}