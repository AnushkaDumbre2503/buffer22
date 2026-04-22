package app;

import model.*;
import engine.*;
import manager.UserStateManager;
import service.MarkovAllocationService;
import java.util.*;

/**
 * Integration guide for adding Markov-based ad allocation to your existing system.
 * 
 * This file shows step-by-step how to integrate the Markov feature with your
 * current AllocationEngine and AdAllocationService.
 */
public class MarkovIntegrationGuide {
    
    /**
     * STEP 1: Update your existing AdAllocationService to use MarkovAllocationService
     * 
     * Original method:
     * public AllocationResult allocateAds(List<Ad> ads, String searchText, 
     *                                     String pageContent, int userId, int sessionId, 
     *                                     Map<Integer, Advertiser> advertisers)
     * 
     * Enhanced method:
     * public AllocationResult allocateAdsWithMarkov(List<Ad> ads, String searchText,
     *                                               String pageContent, int userId, int sessionId,
     *                                               Map<Integer, Advertiser> advertisers,
     *                                               boolean useMarkovOptimization)
     */
    public static class EnhancedAdAllocationService {
        private final AllocationEngine allocationEngine;
        private final ScoringEngine scoringEngine;
        private final MarkovAllocationService markovService;
        
        public EnhancedAdAllocationService(AllocationEngine engine, ScoringEngine scoringEngine) {
            this.allocationEngine = engine;
            this.scoringEngine = scoringEngine;
            this.markovService = new MarkovAllocationService(engine, scoringEngine);
        }
        
        /**
         * Allocate ads with optional Markov optimization.
         * Backward compatible with existing code.
         */
        public AllocationResult allocateAds(List<Ad> ads, String searchText, 
                                           String pageContent, int userId, int sessionId,
                                           Map<Integer, Advertiser> advertisers,
                                           boolean useMarkovOptimization) {
            
            if (useMarkovOptimization) {
                // Use enhanced Markov-based allocation
                return markovService.allocateAdsWithMarkovOptimization(
                    ads, searchText, pageContent, userId, sessionId, advertisers, true
                );
            } else {
                // Use standard allocation (backward compatible)
                return allocationEngine.allocateAds(
                    ads, searchText, pageContent, userId, sessionId, advertisers
                );
            }
        }
    }
    
    /**
     * STEP 2: Update your Main.java or startup code
     */
    public static void setupMarkovSystem() {
        // Initialize Markov components
        AllocationEngine allocationEngine = new AllocationEngine();
        // Initialize ScoringEngine with appropriate parameters
        // (Assuming you have access to memoryWindow in your system)
        
        // Create the enhanced service
        EnhancedAdAllocationService service = new EnhancedAdAllocationService(
            allocationEngine, 
            null  // Pass your ScoringEngine instance
        );
        
        System.out.println("✓ Markov-based ad allocation system initialized");
    }
    
    /**
     * STEP 3: Update your SessionManager to track Markov states
     */
    public static class EnhancedSessionManager {
        private final UserStateManager userStateManager;
        
        public EnhancedSessionManager() {
            this.userStateManager = new UserStateManager();
        }
        
        /**
         * Create session with Markov tracking.
         * Call this when user creates a new session.
         */
        public void createSessionWithMarkov(int sessionId, int userId) {
            // Initialize Markov state for this session
            userStateManager.initializeSession(sessionId, userId, UserIntentState.BROWSING);
        }
        
        /**
         * Get current user intent state.
         */
        public UserIntentState getUserIntentState(int sessionId) {
            return userStateManager.getCurrentState(sessionId);
        }
        
        /**
         * Update state based on user action.
         */
        public void recordUserAction(int sessionId, String action) {
            // Map action to state update
            switch (action.toUpperCase()) {
                case "SEARCH" -> userStateManager.updateCurrentState(
                    sessionId, UserIntentState.SEARCHING
                );
                case "COMPARE" -> userStateManager.updateCurrentState(
                    sessionId, UserIntentState.COMPARING
                );
                case "BUY" -> userStateManager.updateCurrentState(
                    sessionId, UserIntentState.BUYING
                );
            }
        }
    }
    
    /**
     * STEP 4: Example of updating CommandHandler to support Markov commands
     */
    public static class MarkovCommandExample {
        
        /**
         * Sample new commands to add to CommandHandler:
         * 
         * "markov state" - Get current user state
         * "markov predict <sessionId>" - Get next predicted state
         * "markov history <sessionId>" - Get state history
         * "markov stats <sessionId>" - Get detailed Markov stats
         */
        public static void handleMarkovCommand(String command, 
                                              MarkovAllocationService markovService,
                                              int sessionId) {
            String[] parts = command.toLowerCase().split("\\s+");
            
            if (parts.length < 2 || !parts[0].equals("markov")) {
                return;
            }
            
            switch (parts[1]) {
                case "state":
                    System.out.println("Current state: " + 
                        markovService.getUserState(sessionId));
                    break;
                    
                case "predict":
                    MarkovStateEngine.StatePrediction pred = 
                        markovService.getPrediction(sessionId);
                    System.out.println("Prediction: " + pred);
                    break;
                    
                case "history":
                    List<UserIntentState> history = 
                        markovService.getUserStateManager()
                                     .getStateHistory(sessionId);
                    System.out.println("State history: " + history);
                    break;
                    
                case "stats":
                    System.out.println(markovService.getSessionMarkovStats(sessionId));
                    break;
            }
        }
    }
    
    /**
     * STEP 5: Integration example in your ad allocation workflow
     */
    public static AllocationResult allocateAdsWithMarkovWorkflow(
            MarkovAllocationService markovService,
            List<Ad> ads,
            String searchText,
            String pageContent,
            int userId,
            int sessionId,
            Map<Integer, Advertiser> advertisers) {
        
        // 1. Update state based on context
        if (searchText != null && !searchText.isEmpty()) {
            markovService.recordUserAction(sessionId, "SEARCH");
        }
        
        // 2. Get current user intent
        UserIntentState currentIntent = markovService.getUserState(sessionId);
        System.out.println("User intent: " + currentIntent);
        
        // 3. Get next predicted intent
        MarkovStateEngine.StatePrediction prediction = 
            markovService.getPrediction(sessionId);
        System.out.println("Predicted next state: " + 
            prediction.getPredictedState() + " (" + 
            String.format("%.2f%%", prediction.getProbability() * 100) + ")");
        
        // 4. Allocate ads with Markov optimization
        AllocationResult result = markovService.allocateAdsWithMarkovOptimization(
            ads, 
            searchText, 
            pageContent, 
            userId, 
            sessionId, 
            advertisers, 
            true  // Auto-detect state changes
        );
        
        // 5. Log allocation results
        System.out.println("Allocation results: ");
        System.out.println("  Total Ads Allocated: " + result.getTotalAdsAllocated());
        System.out.println("  Total Revenue: $" + String.format("%.2f", result.getTotalRevenue()));
        
        return result;
    }
    
    /**
     * STEP 6: Database schema updates (if storing Markov data)
     * 
     * Add these columns to session table:
     * 
     * ALTER TABLE sessions ADD COLUMN (
     *     current_intent_state VARCHAR(20) DEFAULT 'BROWSING',
     *     previous_intent_state VARCHAR(20),
     *     state_changed_at TIMESTAMP,
     *     state_transition_count INT DEFAULT 0,
     *     state_history JSON
     * );
     * 
     * Add new table for tracking transitions (analytics):
     * 
     * CREATE TABLE state_transitions (
     *     id INT PRIMARY KEY AUTO_INCREMENT,
     *     user_id INT,
     *     session_id INT,
     *     from_state VARCHAR(20),
     *     to_state VARCHAR(20),
     *     transition_time TIMESTAMP,
     *     context VARCHAR(500),
     *     FOREIGN KEY (user_id) REFERENCES users(id),
     *     FOREIGN KEY (session_id) REFERENCES sessions(id)
     * );
     */
    
    /**
     * STEP 7: Metrics and analytics
     */
    public static class MarkovMetricsCollector {
        private final Map<String, Long> stateDistribution = new HashMap<>();
        private final Map<String, Long> transitionCounts = new HashMap<>();
        
        public void recordStateChange(UserIntentState fromState, UserIntentState toState) {
            String transition = fromState.name() + " -> " + toState.name();
            transitionCounts.merge(transition, 1L, Long::sum);
            stateDistribution.merge(toState.name(), 1L, Long::sum);
        }
        
        public void printMetrics() {
            System.out.println("\n=== Markov System Metrics ===");
            System.out.println("State Distribution:");
            stateDistribution.forEach((state, count) -> 
                System.out.println("  " + state + ": " + count)
            );
            
            System.out.println("\nTop Transitions:");
            transitionCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));
        }
    }
    
    /**
     * STEP 8: Configuration (optional)
     * 
     * Create a MarkovConfig class to make the system configurable:
     */
    public static class MarkovConfig {
        private boolean enableMarkovOptimization = true;
        private boolean autoDetectStateChanges = true;
        private boolean logStateTransitions = true;
        private double stateChangeThreshold = 0.5; // min confidence for auto-update
        
        // Getters and setters...
        public boolean isMarkovOptimizationEnabled() { return enableMarkovOptimization; }
        public void setMarkovOptimization(boolean enabled) { enableMarkovOptimization = enabled; }
        
        public boolean isAutoDetectEnabled() { return autoDetectStateChanges; }
        public void setAutoDetect(boolean enabled) { autoDetectStateChanges = enabled; }
        
        public boolean isLoggingEnabled() { return logStateTransitions; }
        public void setLogging(boolean enabled) { logStateTransitions = enabled; }
        
        public double getStateChangeThreshold() { return stateChangeThreshold; }
        public void setStateChangeThreshold(double threshold) { stateChangeThreshold = threshold; }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Markov Integration Guide ===\n");
        System.out.println("See this file for step-by-step integration instructions.");
        System.out.println("\nKey Files Created:");
        System.out.println("1. UserIntentState.java - User states (BROWSING, SEARCHING, COMPARING, BUYING)");
        System.out.println("2. MarkovTransitionMatrix.java - Transition probabilities");
        System.out.println("3. MarkovStateEngine.java - Prediction engine");
        System.out.println("4. UserStateManager.java - Session state management");
        System.out.println("5. MarkovAdTypeRecommender.java - Ad recommendations");
        System.out.println("6. MarkovAllocationService.java - Integration service");
        System.out.println("7. MarkovAllocationExample.java - Usage examples");
        System.out.println("8. MARKOV_SYSTEM_README.md - Complete documentation");
    }
}