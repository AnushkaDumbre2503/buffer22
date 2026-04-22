package model;

/**
 * Represents the user's current intent state in the purchase journey.
 * Used by the Markov-based ad allocation engine to predict user behavior.
 */
public enum UserIntentState {
    /**
     * User is casually exploring content without specific intent.
     * Typical actions: browsing multiple categories, scrolling, viewing products passively.
     */
    BROWSING(1),
    
    /**
     * User is actively searching for specific products or information.
     * Typical actions: using search bar, filtering results, reading descriptions.
     */
    SEARCHING(2),
    
    /**
     * User is comparing alternatives and evaluating options.
     * Typical actions: comparing prices, reading reviews, checking specifications.
     */
    COMPARING(3),
    
    /**
     * User is ready to make a purchase or take action.
     * Typical actions: adding to cart, viewing checkout, clicking "Buy Now".
     */
    BUYING(4);
    
    private final int priority;
    
    UserIntentState(int priority) {
        this.priority = priority;
    }
    
    public int getPriority() {
        return priority;
    }
    
    /**
     * Get the default ad type to show for this state.
     */
    public String getRecommendedAdType() {
        return switch (this) {
            case BROWSING -> "awareness";
            case SEARCHING -> "category";
            case COMPARING -> "comparison";
            case BUYING -> "conversion";
        };
    }
    
    /**
     * Get the recommended CTR boost factor for this state.
     */
    public double getCTRBoostFactor() {
        return switch (this) {
            case BROWSING -> 1.0;
            case SEARCHING -> 1.3;
            case COMPARING -> 1.6;
            case BUYING -> 2.0;
        };
    }
}