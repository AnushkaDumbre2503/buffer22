package engine;

import model.UserIntentState;
import model.MarkovTransitionMatrix;
import java.util.*;

/**
 * Markov-based state prediction engine.
 * Predicts the next user intent state based on current state and transition probabilities.
 * 
 * Implements the Markov property: P(Sₜ₊₁ | Sₜ) depends only on current state Sₜ.
 */
public class MarkovStateEngine {
    private final MarkovTransitionMatrix transitionMatrix;
    private final Random random;
    
    public MarkovStateEngine() {
        this.transitionMatrix = new MarkovTransitionMatrix();
        this.random = new Random();
    }
    
    public MarkovStateEngine(MarkovTransitionMatrix customMatrix) {
        this.transitionMatrix = customMatrix;
        this.random = new Random();
    }
    
    /**
     * Predict the next user intent state based on current state.
     * Uses probabilistic transitions to determine the most likely next state.
     * 
     * @param currentState The user's current intent state
     * @return Prediction containing next state and probability
     */
    public StatePrediction predictNextState(UserIntentState currentState) {
        if (currentState == null) {
            // Default to browsing if no state provided
            currentState = UserIntentState.BROWSING;
        }
        
        // Get all possible transitions from current state
        Map<UserIntentState, Double> transitions = transitionMatrix.getTransitionsFrom(currentState);
        
        // Find the state with highest probability
        UserIntentState predictedState = transitions.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(currentState);
        
        double probability = transitions.getOrDefault(predictedState, 0.0);
        
        return new StatePrediction(currentState, predictedState, probability);
    }
    
    /**
     * Probabilistically sample the next state (useful for simulations).
     * 
     * @param currentState The user's current intent state
     * @return Next state sampled according to transition probabilities
     */
    public UserIntentState sampleNextState(UserIntentState currentState) {
        if (currentState == null) {
            currentState = UserIntentState.BROWSING;
        }
        
        Map<UserIntentState, Double> transitions = transitionMatrix.getTransitionsFrom(currentState);
        
        double rand = random.nextDouble();
        double cumulative = 0.0;
        
        for (Map.Entry<UserIntentState, Double> entry : transitions.entrySet()) {
            cumulative += entry.getValue();
            if (rand <= cumulative) {
                return entry.getKey();
            }
        }
        
        // Fallback to highest probability state
        return transitions.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(currentState);
    }
    
    /**
     * Get all possible next states ranked by probability.
     * 
     * @param currentState The user's current intent state
     * @return List of states ranked by transition probability (highest first)
     */
    public List<StatePrediction> getRankedPredictions(UserIntentState currentState) {
        if (currentState == null) {
            currentState = UserIntentState.BROWSING;
        }
        
        final UserIntentState finalCurrentState = currentState;
        Map<UserIntentState, Double> transitions = transitionMatrix.getTransitionsFrom(currentState);
        List<StatePrediction> predictions = new ArrayList<>();
        
        transitions.forEach((nextState, probability) -> {
            predictions.add(new StatePrediction(finalCurrentState, nextState, probability));
        });
        
        // Sort by probability descending
        predictions.sort((a, b) -> Double.compare(b.getProbability(), a.getProbability()));
        
        return predictions;
    }
    
    /**
     * Get the transition probability between two states directly.
     */
    public double getTransitionProbability(UserIntentState fromState, UserIntentState toState) {
        return transitionMatrix.getTransitionProbability(fromState, toState);
    }
    
    /**
     * Update transition probability (for learning/adaptation).
     */
    public void updateTransitionProbability(UserIntentState fromState, UserIntentState toState, double probability) {
        transitionMatrix.setTransitionProbability(fromState, toState, probability);
    }
    
    /**
     * Get the underlying transition matrix.
     */
    public MarkovTransitionMatrix getTransitionMatrix() {
        return transitionMatrix;
    }
    
    /**
     * Inner class representing a state prediction result.
     */
    public static class StatePrediction {
        private final UserIntentState currentState;
        private final UserIntentState predictedState;
        private final double probability;
        
        public StatePrediction(UserIntentState currentState, UserIntentState predictedState, double probability) {
            this.currentState = currentState;
            this.predictedState = predictedState;
            this.probability = probability;
        }
        
        public UserIntentState getCurrentState() {
            return currentState;
        }
        
        public UserIntentState getPredictedState() {
            return predictedState;
        }
        
        public double getProbability() {
            return probability;
        }
        
        @Override
        public String toString() {
            return String.format("Prediction: %s -> %s (prob: %.2f%%)", 
                currentState.name(), predictedState.name(), probability * 100);
        }
    }
}