package model;

import java.util.*;

/**
 * Represents the transition probability matrix for user intent states.
 * Stores P(Sₜ₊₁ | Sₜ) - probability of next state given current state.
 */
public class MarkovTransitionMatrix {
    private final Map<UserIntentState, Map<UserIntentState, Double>> transitionMatrix;
    
    /**
     * Default transition probabilities based on typical user behavior patterns.
     */
    private static final double[][] DEFAULT_PROBABILITIES = {
        // From\To    BROWSING  SEARCHING  COMPARING  BUYING
        /*BROWSING*/   {0.4,      0.6,      0.0,       0.0},
        /*SEARCHING*/  {0.1,      0.2,      0.7,       0.0},
        /*COMPARING*/  {0.05,     0.05,     0.1,       0.8},
        /*BUYING*/     {0.0,      0.0,      0.0,       1.0}
    };
    
    public MarkovTransitionMatrix() {
        this.transitionMatrix = new HashMap<>();
        initializeDefaultMatrix();
    }
    
    /**
     * Initialize with default transition probabilities.
     */
    private void initializeDefaultMatrix() {
        UserIntentState[] states = UserIntentState.values();
        
        for (int i = 0; i < states.length; i++) {
            UserIntentState fromState = states[i];
            Map<UserIntentState, Double> transitions = new HashMap<>();
            
            for (int j = 0; j < states.length; j++) {
                UserIntentState toState = states[j];
                transitions.put(toState, DEFAULT_PROBABILITIES[i][j]);
            }
            
            transitionMatrix.put(fromState, transitions);
        }
    }
    
    /**
     * Get the transition probability from one state to another.
     * 
     * @param fromState Current user state
     * @param toState Target user state
     * @return Probability of transition (0.0 to 1.0)
     */
    public double getTransitionProbability(UserIntentState fromState, UserIntentState toState) {
        return transitionMatrix
            .getOrDefault(fromState, new HashMap<>())
            .getOrDefault(toState, 0.0);
    }
    
    /**
     * Get all transition probabilities from a given state.
     * 
     * @param fromState Current user state
     * @return Map of target states to probabilities
     */
    public Map<UserIntentState, Double> getTransitionsFrom(UserIntentState fromState) {
        return new HashMap<>(transitionMatrix.getOrDefault(fromState, new HashMap<>()));
    }
    
    /**
     * Update a transition probability.
     * Used for learning and adapting to new behavior patterns.
     * 
     * @param fromState Current state
     * @param toState Next state
     * @param probability New probability value
     */
    public void setTransitionProbability(UserIntentState fromState, UserIntentState toState, double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("Probability must be between 0.0 and 1.0");
        }
        
        transitionMatrix
            .computeIfAbsent(fromState, k -> new HashMap<>())
            .put(toState, probability);
    }
    
    /**
     * Normalize all transition probabilities from a given state to ensure they sum to 1.0.
     */
    public void normalizeTransitions(UserIntentState fromState) {
        Map<UserIntentState, Double> transitions = transitionMatrix.get(fromState);
        if (transitions == null || transitions.isEmpty()) {
            return;
        }
        
        double sum = transitions.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
        
        if (sum > 0) {
            transitions.forEach((state, prob) -> 
                transitions.put(state, prob / sum)
            );
        }
    }
    
    /**
     * Get a string representation of the transition matrix.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Markov Transition Matrix:\n");
        UserIntentState[] states = UserIntentState.values();
        
        // Header
        sb.append("From\\To");
        for (UserIntentState state : states) {
            sb.append(String.format("%12s", state.name()));
        }
        sb.append("\n");
        
        // Rows
        for (UserIntentState fromState : states) {
            sb.append(String.format("%-10s", fromState.name()));
            Map<UserIntentState, Double> transitions = transitionMatrix.get(fromState);
            for (UserIntentState toState : states) {
                double prob = transitions.getOrDefault(toState, 0.0);
                sb.append(String.format("%12.4f", prob));
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}