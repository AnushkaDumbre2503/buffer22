package app;

import model.*;
import engine.*;
import manager.UserStateManager;
import service.MarkovAllocationService;

/**
 * Example demonstrating how to use the Markov-based ad allocation system.
 * 
 * This example shows:
 * 1. Initializing the Markov system
 * 2. Tracking user states
 * 3. Making predictions
 * 4. Allocating ads based on predictions
 */
public class MarkovAllocationExample {
    
    public static void main(String[] args) {
        System.out.println("=== Markov-Based Ad Allocation Engine Demo ===\n");
        
        // Initialize components
        AllocationEngine allocationEngine = new AllocationEngine();
        ScoringEngine scoringEngine = new ScoringEngine(null);
        MarkovAllocationService markovService = new MarkovAllocationService(allocationEngine, scoringEngine);
        
        // Scenario: Tracking user journey through intent states
        demonstrateUserJourney(markovService);
        
        System.out.println("\n=== Markov Transition Probabilities ===\n");
        demonstrateTransitionProbabilities(markovService);
        
        System.out.println("\n=== Ad Type Recommendations ===\n");
        demonstrateAdRecommendations(markovService);
    }
    
    /**
     * Demonstrate how user journeys flow through intent states.
     */
    private static void demonstrateUserJourney(MarkovAllocationService markovService) {
        int userId = 101;
        int sessionId = 1001;
        
        // Initialize session (user starts browsing)
        markovService.getUserStateManager().initializeSession(sessionId, userId, UserIntentState.BROWSING);
        System.out.println("Session " + sessionId + " initialized");
        System.out.println("Initial State: " + markovService.getUserState(sessionId));
        
        // User performs search action
        System.out.println("\n--- User performs search ---");
        markovService.recordUserAction(sessionId, "SEARCH");
        printStateInfo(markovService, sessionId);
        
        // User compares products
        System.out.println("\n--- User compares products ---");
        markovService.recordUserAction(sessionId, "COMPARE");
        printStateInfo(markovService, sessionId);
        
        // User clicks ads and adds to cart
        System.out.println("\n--- User adds to cart ---");
        markovService.recordUserAction(sessionId, "ADD_TO_CART");
        printStateInfo(markovService, sessionId);
        
        // User completes purchase
        System.out.println("\n--- User completes purchase ---");
        markovService.recordUserAction(sessionId, "PURCHASE");
        printStateInfo(markovService, sessionId);
        
        // End session
        markovService.endSession(sessionId);
    }
    
    /**
     * Print detailed state information.
     */
    private static void printStateInfo(MarkovAllocationService markovService, int sessionId) {
        UserIntentState currentState = markovService.getUserState(sessionId);
        MarkovStateEngine.StatePrediction prediction = markovService.getPrediction(sessionId);
        
        System.out.println("  Current State: " + currentState.name());
        System.out.println("  Predicted Next State: " + prediction.getPredictedState().name() + 
                         String.format(" (%.2f%% probability)", prediction.getProbability() * 100));
        
        // Show recommended ad type
        MarkovAdTypeRecommender recommender = markovService.getAdTypeRecommender();
        MarkovAdTypeRecommender.AdTypeCategory recommendedType = 
            recommender.recommendAdType(prediction.getPredictedState());
        System.out.println("  Recommended Ad Type: " + recommendedType.getName() + 
                         " (" + recommendedType.getDescription() + ")");
        
        System.out.println("  CTR Boost Factor: " + String.format("%.2f", recommender.getCTRBoostFactor(prediction.getPredictedState())));
        System.out.println("  Bid Adjustment Factor: " + String.format("%.2f", recommender.getBidAdjustmentFactor(prediction.getPredictedState())));
    }
    
    /**
     * Demonstrate the Markov transition probabilities.
     */
    private static void demonstrateTransitionProbabilities(MarkovAllocationService markovService) {
        UserStateManager userStateManager = markovService.getUserStateManager();
        MarkovStateEngine markovEngine = userStateManager.getMarkovEngine();
        
        // Show transition matrix
        System.out.println(markovEngine.getTransitionMatrix().toString());
        
        // Show specific transition probabilities
        System.out.println("\nKey Transition Probabilities:");
        System.out.println("  P(BROWSING → SEARCHING) = " + 
            String.format("%.2f", markovEngine.getTransitionProbability(UserIntentState.BROWSING, UserIntentState.SEARCHING)));
        System.out.println("  P(SEARCHING → COMPARING) = " + 
            String.format("%.2f", markovEngine.getTransitionProbability(UserIntentState.SEARCHING, UserIntentState.COMPARING)));
        System.out.println("  P(COMPARING → BUYING) = " + 
            String.format("%.2f", markovEngine.getTransitionProbability(UserIntentState.COMPARING, UserIntentState.BUYING)));
    }
    
    /**
     * Demonstrate ad recommendations for each state.
     */
    private static void demonstrateAdRecommendations(MarkovAllocationService markovService) {
        MarkovAdTypeRecommender recommender = markovService.getAdTypeRecommender();
        
        System.out.println("Ad Recommendations by User State:\n");
        
        for (UserIntentState state : UserIntentState.values()) {
            System.out.println("State: " + state.name());
            System.out.println("  Recommended Ad Type: " + recommender.recommendAdType(state).getName());
            System.out.println("  Message Tone: " + recommender.getMessageTone(state));
            System.out.println("  CTR Boost: " + String.format("%.2f", recommender.getCTRBoostFactor(state)) + "x");
            System.out.println("  Bid Adjustment: " + String.format("%.2f", recommender.getBidAdjustmentFactor(state)) + "x");
            System.out.println();
        }
    }
}