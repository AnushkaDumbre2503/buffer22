package utils;

public class Constants {
    // Database configuration
    public static final String DB_URL = "jdbc:mysql://localhost:3306/ad_allocation_engine?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "root";

    // Engine configuration
    public static final int MEMORY_WINDOW_SIZE = 1000;
    public static final double MIN_SCORE = 0.01;
    public static final double BASE_SCORE = 1.0;

    // Slot configuration
    public static final int TOP_SLOT_COOLDOWN_SECONDS = 3;
    public static final int SIDEBAR_SLOT_COOLDOWN_SECONDS = 2;
    public static final int FOOTER_SLOT_COOLDOWN_SECONDS = 1;

    // Scoring weights
    public static final double BID_WEIGHT = 1.0;
    public static final double CTR_WEIGHT = 1.0;
    public static final double CONTEXT_WEIGHT = 1.0;
    public static final double MEMORY_BOOST_WEIGHT = 1.0;
    public static final double FATIGUE_CONTROL_WEIGHT = 1.0;

    // Budget pacing
    public static final double PACING_FACTOR = 0.8;
    public static final int PACING_PERIOD_HOURS = 24;

    // Session configuration
    public static final int SESSION_DURATION_HOURS = 24;
    public static final int MAX_SESSIONS_PER_USER = 5;

    // Performance tracking
    public static final int RECENT_ACTIVITY_MINUTES = 10;
    public static final int POPULAR_SEARCHES_LIMIT = 10;

    // Simulation configuration
    public static final int SIMULATION_DAYS = 7;
    public static final double DEFAULT_BID_AMOUNT = 2.50;

    // CLI configuration
    public static final String APP_NAME = "Intelligent Ad Allocation Engine";
    public static final String VERSION = "1.0.0";

    // Error messages
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_ADVERTISER_NOT_FOUND = "Advertiser not found";
    public static final String ERROR_AD_NOT_FOUND = "Ad not found";
    public static final String ERROR_INSUFFICIENT_BUDGET = "Insufficient budget";
    public static final String ERROR_INVALID_INPUT = "Invalid input";

    // Success messages
    public static final String SUCCESS_LOGIN = "Login successful";
    public static final String SUCCESS_REGISTRATION = "Registration successful";
    public static final String SUCCESS_ALLOCATION = "Ads allocated successfully";
    public static final String SUCCESS_CLICK_RECORDED = "Click recorded successfully";

    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
}
