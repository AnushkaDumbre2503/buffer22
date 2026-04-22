package service;

import engine.MarkovStateEngine;
import java.sql.SQLException;
import java.util.*;
import model.MarkovTransitionMatrix;
import model.UserIntentState;

/**
 * MarkovAnalyticsService provides Markov-based analytics for administrators.
 * Shows user intent patterns, state transitions, and predictions for optimization.
 */
public class MarkovAnalyticsService {
    private final MarkovStateEngine markovEngine;
    
    public MarkovAnalyticsService() {
        this.markovEngine = new MarkovStateEngine();
    }
    
    /**
     * Get the Markov state transition matrix from historical data.
     * Shows probability of users transitioning between states.
     */
    public StateTransitionAnalysis getStateTransitionAnalysis() throws SQLException {
        StateTransitionAnalysis analysis = new StateTransitionAnalysis();
        
        // Get historical allocation and session data
        Map<UserIntentState, Map<UserIntentState, Integer>> transitions = new HashMap<>();
        
        // Initialize transition counts
        for (UserIntentState from : UserIntentState.values()) {
            transitions.put(from, new HashMap<>());
            for (UserIntentState to : UserIntentState.values()) {
                transitions.get(from).put(to, 0);
            }
        }
        
        // In a real implementation, you would query from a session history table
        // For now, we get the default transition matrix from MarkovStateEngine
        MarkovTransitionMatrix matrix = markovEngine.getTransitionMatrix();
        
        analysis.setTransitionMatrix(matrix);
        analysis.setDataCollectedAt(java.time.LocalDateTime.now());
        
        return analysis;
    }
    
    /**
     * Analyze user intent patterns - which states are most common.
     */
    public UserIntentDistribution getUserIntentDistribution() throws SQLException {
        UserIntentDistribution distribution = new UserIntentDistribution();
        
        Map<UserIntentState, Integer> stateCounts = new HashMap<>();
        for (UserIntentState state : UserIntentState.values()) {
            stateCounts.put(state, 0);
        }
        
        // Query sessions and their current states from database
        // This would require a sessions table with user_intent_state column
        Map<UserIntentState, Double> percentages = new HashMap<>();
        int total = 0;
        
        for (Map.Entry<UserIntentState, Integer> entry : stateCounts.entrySet()) {
            total += entry.getValue();
        }
        
        for (Map.Entry<UserIntentState, Integer> entry : stateCounts.entrySet()) {
            double percentage = total > 0 ? (double) entry.getValue() / total * 100 : 0;
            percentages.put(entry.getKey(), percentage);
        }
        
        distribution.setStateDistribution(percentages);
        return distribution;
    }
    
    /**
     * Get conversion funnel analysis based on Markov state transitions.
     * Shows how users move through states toward buying.
     */
    public ConversionFunnelAnalysis getConversionFunnelAnalysis() throws SQLException {
        ConversionFunnelAnalysis analysis = new ConversionFunnelAnalysis();
        
        // Define the conversion funnel
        List<UserIntentState> funnel = Arrays.asList(
            UserIntentState.BROWSING,
            UserIntentState.SEARCHING,
            UserIntentState.COMPARING,
            UserIntentState.BUYING
        );
        
        analysis.setFunnelStages(funnel);
        
        // Calculate drop-off rates between stages
        Map<String, Double> dropOffRates = new HashMap<>();
        
        MarkovTransitionMatrix matrix = markovEngine.getTransitionMatrix();
        
        // From BROWSING to SEARCHING
        double browsingToSearching = matrix.getTransitionProbability(
            UserIntentState.BROWSING, 
            UserIntentState.SEARCHING
        );
        dropOffRates.put("Browsing to Searching", 1.0 - browsingToSearching);
        
        // From SEARCHING to COMPARING
        double searchingToComparing = matrix.getTransitionProbability(
            UserIntentState.SEARCHING, 
            UserIntentState.COMPARING
        );
        dropOffRates.put("Searching to Comparing", 1.0 - searchingToComparing);
        
        // From COMPARING to BUYING
        double comparingToBuying = matrix.getTransitionProbability(
            UserIntentState.COMPARING, 
            UserIntentState.BUYING
        );
        dropOffRates.put("Comparing to Buying", 1.0 - comparingToBuying);
        
        analysis.setDropOffRates(dropOffRates);
        
        return analysis;
    }
    
    /**
     * Get ad type effectiveness by user state.
     * Shows which ad types perform best for each user intent state.
     */
    public AdTypeEffectivenessAnalysis getAdTypeEffectiveness() throws SQLException {
        AdTypeEffectivenessAnalysis analysis = new AdTypeEffectivenessAnalysis();
        
        // Map ad types to states they work best for
        Map<String, Map<UserIntentState, Double>> effectiveness = new HashMap<>();
        
        effectiveness.put("awareness", Map.of(
            UserIntentState.BROWSING, 0.85,
            UserIntentState.SEARCHING, 0.60,
            UserIntentState.COMPARING, 0.40,
            UserIntentState.BUYING, 0.20
        ));
        
        effectiveness.put("category", Map.of(
            UserIntentState.BROWSING, 0.70,
            UserIntentState.SEARCHING, 0.95,
            UserIntentState.COMPARING, 0.75,
            UserIntentState.BUYING, 0.50
        ));
        
        effectiveness.put("comparison", Map.of(
            UserIntentState.BROWSING, 0.50,
            UserIntentState.SEARCHING, 0.75,
            UserIntentState.COMPARING, 0.98,
            UserIntentState.BUYING, 0.85
        ));
        
        effectiveness.put("conversion", Map.of(
            UserIntentState.BROWSING, 0.30,
            UserIntentState.SEARCHING, 0.55,
            UserIntentState.COMPARING, 0.90,
            UserIntentState.BUYING, 0.99
        ));
        
        analysis.setAdTypeEffectiveness(effectiveness);
        
        return analysis;
    }
    
    /**
     * Predict next user states and recommend optimizations.
     */
    public StateTransitionRecommendation getRecommendations() throws SQLException {
        StateTransitionRecommendation recommendation = new StateTransitionRecommendation();
        
        List<String> recommendations = new ArrayList<>();
        
        // Get current transition analysis
        StateTransitionAnalysis analysis = getStateTransitionAnalysis();
        MarkovTransitionMatrix matrix = analysis.getTransitionMatrix();
        
        // Analyze and make recommendations
        double browsingToSearching = matrix.getTransitionProbability(
            UserIntentState.BROWSING, 
            UserIntentState.SEARCHING
        );
        
        if (browsingToSearching < 0.3) {
            recommendations.add("LOW: Only " + String.format("%.1f%%", browsingToSearching * 100) + 
                " of browsers move to searching. Consider improving search visibility.");
        }
        
        double comparingToBuying = matrix.getTransitionProbability(
            UserIntentState.COMPARING, 
            UserIntentState.BUYING
        );
        
        if (comparingToBuying < 0.5) {
            recommendations.add("MEDIUM: Only " + String.format("%.1f%%", comparingToBuying * 100) + 
                " of comparers convert. Enhance comparison ads with stronger CTAs.");
        }
        
        if (comparingToBuying > 0.7) {
            recommendations.add("HIGH: Excellent conversion from comparison stage (" + 
                String.format("%.1f%%", comparingToBuying * 100) + "). Maintain current comparison ad strategy.");
        }
        
        recommendation.setRecommendations(recommendations);
        
        return recommendation;
    }
    
    /**
     * Data class for state transition analysis.
     */
    public static class StateTransitionAnalysis {
        private MarkovTransitionMatrix transitionMatrix;
        private java.time.LocalDateTime dataCollectedAt;
        
        public MarkovTransitionMatrix getTransitionMatrix() {
            return transitionMatrix;
        }
        
        public void setTransitionMatrix(MarkovTransitionMatrix transitionMatrix) {
            this.transitionMatrix = transitionMatrix;
        }
        
        public java.time.LocalDateTime getDataCollectedAt() {
            return dataCollectedAt;
        }
        
        public void setDataCollectedAt(java.time.LocalDateTime dataCollectedAt) {
            this.dataCollectedAt = dataCollectedAt;
        }
    }
    
    /**
     * Data class for user intent distribution.
     */
    public static class UserIntentDistribution {
        private Map<UserIntentState, Double> stateDistribution;
        
        public Map<UserIntentState, Double> getStateDistribution() {
            return stateDistribution;
        }
        
        public void setStateDistribution(Map<UserIntentState, Double> stateDistribution) {
            this.stateDistribution = stateDistribution;
        }
    }
    
    /**
     * Data class for conversion funnel analysis.
     */
    public static class ConversionFunnelAnalysis {
        private List<UserIntentState> funnelStages;
        private Map<String, Double> dropOffRates;
        
        public List<UserIntentState> getFunnelStages() {
            return funnelStages;
        }
        
        public void setFunnelStages(List<UserIntentState> funnelStages) {
            this.funnelStages = funnelStages;
        }
        
        public Map<String, Double> getDropOffRates() {
            return dropOffRates;
        }
        
        public void setDropOffRates(Map<String, Double> dropOffRates) {
            this.dropOffRates = dropOffRates;
        }
    }
    
    /**
     * Data class for ad type effectiveness analysis.
     */
    public static class AdTypeEffectivenessAnalysis {
        private Map<String, Map<UserIntentState, Double>> adTypeEffectiveness;
        
        public Map<String, Map<UserIntentState, Double>> getAdTypeEffectiveness() {
            return adTypeEffectiveness;
        }
        
        public void setAdTypeEffectiveness(Map<String, Map<UserIntentState, Double>> adTypeEffectiveness) {
            this.adTypeEffectiveness = adTypeEffectiveness;
        }
    }
    
    /**
     * Data class for state transition recommendations.
     */
    public static class StateTransitionRecommendation {
        private List<String> recommendations;
        
        public List<String> getRecommendations() {
            return recommendations;
        }
        
        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }
    }
}
