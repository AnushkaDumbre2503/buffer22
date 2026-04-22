package manager;

import model.UserIntentState;
import engine.MarkovStateEngine;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Manages user intent states per session.
 * Tracks the current state of each user and predicts state transitions using Markov model.
 */
public class UserStateManager {
    private final MarkovStateEngine markovEngine;
    private final Map<Integer, UserSessionState> sessionStates; // sessionId -> state
    private final Map<Integer, UserIntentState> userDefaultStates; // userId -> default state
    
    /**
     * Tracks state information per session.
     */
    private static class UserSessionState {
        UserIntentState currentState;
        UserIntentState previousState;
        LocalDateTime stateChangedAt;
        int stateTransitionCount;
        List<UserIntentState> stateHistory;
        
        UserSessionState(UserIntentState initialState) {
            this.currentState = initialState;
            this.previousState = null;
            this.stateChangedAt = LocalDateTime.now();
            this.stateTransitionCount = 0;
            this.stateHistory = new ArrayList<>();
            this.stateHistory.add(initialState);
        }
    }
    
    public UserStateManager() {
        this.markovEngine = new MarkovStateEngine();
        this.sessionStates = new HashMap<>();
        this.userDefaultStates = new HashMap<>();
    }
    
    /**
     * Initialize a session with a starting state.
     * 
     * @param sessionId Unique session identifier
     * @param userId User ID associated with the session
     * @param initialState Starting user intent state
     */
    public void initializeSession(int sessionId, int userId, UserIntentState initialState) {
        if (initialState == null) {
            initialState = UserIntentState.BROWSING;
        }
        sessionStates.put(sessionId, new UserSessionState(initialState));
        userDefaultStates.put(userId, initialState);
    }
    
    /**
     * Initialize a session with default browsing state.
     */
    public void initializeSession(int sessionId, int userId) {
        initializeSession(sessionId, userId, UserIntentState.BROWSING);
    }
    
    /**
     * Get the current state of a user in a session.
     * 
     * @param sessionId Session ID
     * @return Current user intent state, or BROWSING if session not found
     */
    public UserIntentState getCurrentState(int sessionId) {
        UserSessionState state = sessionStates.get(sessionId);
        return state != null ? state.currentState : UserIntentState.BROWSING;
    }
    
    /**
     * Update the current state of a user in a session.
     * This manually changes the user's state (e.g., when user explicitly searches).
     * 
     * @param sessionId Session ID
     * @param newState New user intent state
     * @return Updated state
     */
    public UserIntentState updateCurrentState(int sessionId, UserIntentState newState) {
        UserSessionState state = sessionStates.get(sessionId);
        if (state == null) {
            initializeSession(sessionId, -1, newState);
            state = sessionStates.get(sessionId);
        }
        
        if (!state.currentState.equals(newState)) {
            state.previousState = state.currentState;
            state.currentState = newState;
            state.stateChangedAt = LocalDateTime.now();
            state.stateTransitionCount++;
            state.stateHistory.add(newState);
        }
        
        return state.currentState;
    }
    
    /**
     * Predict the next user intent state using the Markov model.
     * 
     * @param sessionId Session ID
     * @return Predicted next state
     */
    public UserIntentState predictNextState(int sessionId) {
        UserIntentState currentState = getCurrentState(sessionId);
        MarkovStateEngine.StatePrediction prediction = markovEngine.predictNextState(currentState);
        return prediction.getPredictedState();
    }
    
    /**
     * Get detailed prediction with probability.
     * 
     * @param sessionId Session ID
     * @return State prediction with probability score
     */
    public MarkovStateEngine.StatePrediction getPredictionWithProbability(int sessionId) {
        UserIntentState currentState = getCurrentState(sessionId);
        return markovEngine.predictNextState(currentState);
    }
    
    /**
     * Get all ranked predictions (all possible next states ordered by probability).
     * 
     * @param sessionId Session ID
     * @return List of predictions ranked by probability
     */
    public List<MarkovStateEngine.StatePrediction> getRankedPredictions(int sessionId) {
        UserIntentState currentState = getCurrentState(sessionId);
        return markovEngine.getRankedPredictions(currentState);
    }
    
    /**
     * Get state transition count for a session.
     * Useful for understanding user journey complexity.
     * 
     * @param sessionId Session ID
     * @return Number of state transitions in this session
     */
    public int getStateTransitionCount(int sessionId) {
        UserSessionState state = sessionStates.get(sessionId);
        return state != null ? state.stateTransitionCount : 0;
    }
    
    /**
     * Get state history for a session (all states visited in order).
     * 
     * @param sessionId Session ID
     * @return Ordered list of states visited in session
     */
    public List<UserIntentState> getStateHistory(int sessionId) {
        UserSessionState state = sessionStates.get(sessionId);
        return state != null ? new ArrayList<>(state.stateHistory) : new ArrayList<>();
    }
    
    /**
     * Get previous state before current state.
     * 
     * @param sessionId Session ID
     * @return Previous state, or null if no previous state
     */
    public UserIntentState getPreviousState(int sessionId) {
        UserSessionState state = sessionStates.get(sessionId);
        return state != null ? state.previousState : null;
    }
    
    /**
     * Get time since last state change.
     * 
     * @param sessionId Session ID
     * @return Duration in seconds since last state transition
     */
    public long getSecondsSinceStateChange(int sessionId) {
        UserSessionState state = sessionStates.get(sessionId);
        if (state == null) return 0L;
        
        return java.time.temporal.ChronoUnit.SECONDS
            .between(state.stateChangedAt, LocalDateTime.now());
    }
    
    /**
     * Detect potential state change based on user behavior signals.
     * This is a helper method for the system to suggest state updates.
     * 
     * @param sessionId Session ID
     * @param behaviorSignals Map of behavior signals (e.g., "hasSearched", "viewingComparisons")
     * @return Suggested state update, or current state if no change recommended
     */
    public UserIntentState suggestStateUpdate(int sessionId, Map<String, Boolean> behaviorSignals) {
        UserIntentState currentState = getCurrentState(sessionId);
        
        // If user is searching
        if (behaviorSignals.getOrDefault("hasSearched", false)) {
            if (currentState == UserIntentState.BROWSING) {
                return UserIntentState.SEARCHING;
            }
        }
        
        // If user is comparing products
        if (behaviorSignals.getOrDefault("isComparing", false)) {
            if (currentState.getPriority() < UserIntentState.COMPARING.getPriority()) {
                return UserIntentState.COMPARING;
            }
        }
        
        // If user is in checkout or has clicked buy
        if (behaviorSignals.getOrDefault("inCheckout", false) || 
            behaviorSignals.getOrDefault("clickedBuy", false)) {
            if (currentState.getPriority() < UserIntentState.BUYING.getPriority()) {
                return UserIntentState.BUYING;
            }
        }
        
        return currentState;
    }
    
    /**
     * End a session and clean up its state.
     * 
     * @param sessionId Session ID
     */
    public void endSession(int sessionId) {
        sessionStates.remove(sessionId);
    }
    
    /**
     * Get the Markov engine for direct access if needed.
     */
    public MarkovStateEngine getMarkovEngine() {
        return markovEngine;
    }
    
    /**
     * Get session statistics as a string.
     */
    public String getSessionStats(int sessionId) {
        UserSessionState state = sessionStates.get(sessionId);
        if (state == null) {
            return "Session not found";
        }
        
        return String.format(
            "Session %d Stats: CurrentState=%s, Transitions=%d, History=%s, SecondsSinceChange=%d",
            sessionId,
            state.currentState.name(),
            state.stateTransitionCount,
            state.stateHistory,
            getSecondsSinceStateChange(sessionId)
        );
    }
}