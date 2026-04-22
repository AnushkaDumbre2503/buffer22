package logs;

import model.AllocationEvent;
import model.AllocationResult;
import model.User;
import model.Advertiser;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventLogger {
    private static final String LOG_FILE = "ad_allocation_events.log";
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static EventLogger instance;
    
    private EventLogger() {}
    
    public static synchronized EventLogger getInstance() {
        if (instance == null) {
            instance = new EventLogger();
        }
        return instance;
    }
    
    public void logUserLogin(User user) {
        logEvent("USER_LOGIN", "User logged in: " + user.getUsername() + " (ID: " + user.getId() + ")");
    }
    
    public void logUserRegistration(User user) {
        logEvent("USER_REGISTRATION", "New user registered: " + user.getUsername() + " (ID: " + user.getId() + ")");
    }
    
    public void logAllocation(AllocationResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ad allocation completed for user ").append(result.getUserId())
          .append(" (Session: ").append(result.getSessionId()).append(")");
        sb.append(" | Ads allocated: ").append(result.getTotalAdsAllocated());
        sb.append(" | Revenue: $").append(String.format("%.2f", result.getTotalRevenue()));
        
        logEvent("AD_ALLOCATION", sb.toString());
    }
    
    public void logClick(AllocationEvent event) {
        logEvent("AD_CLICK", "Ad clicked: " + event.getAdId() + 
                 " (User: " + event.getUserId() + ", Slot: " + event.getSlotType().getName() + 
                 ", Price: $" + String.format("%.2f", event.getFinalPrice()) + ")");
    }
    
    public void logAdvertiserCreation(Advertiser advertiser) {
        logEvent("ADVERTISER_CREATION", "New advertiser created: " + advertiser.getName() + 
                 " (ID: " + advertiser.getId() + ", Budget: $" + String.format("%.2f", advertiser.getTotalBudget()) + ")");
    }
    
    public void logBudgetUpdate(Advertiser advertiser, double oldBudget, double newBudget) {
        logEvent("BUDGET_UPDATE", "Budget updated for advertiser " + advertiser.getName() + 
                 " (ID: " + advertiser.getId() + "): $" + String.format("%.2f", oldBudget) + 
                 " -> $" + String.format("%.2f", newBudget));
    }
    
    public void logConflictAdded(int advertiser1Id, int advertiser2Id) {
        logEvent("CONFLICT_ADDED", "Conflict added between advertisers " + advertiser1Id + " and " + advertiser2Id);
    }
    
    public void logSystemStart() {
        logEvent("SYSTEM_START", "Ad Allocation Engine started successfully");
    }
    
    public void logSystemShutdown() {
        logEvent("SYSTEM_SHUTDOWN", "Ad Allocation Engine shutting down");
    }
    
    public void logDatabaseConnection(String status) {
        logEvent("DATABASE", "Database connection: " + status);
    }
    
    public void logError(String errorMessage, Exception e) {
        logEvent("ERROR", errorMessage + " | Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
    
    public void logPerformanceMetric(String metric, double value) {
        logEvent("PERFORMANCE", metric + ": " + String.format("%.4f", value));
    }
    
    public void logRevenue(double revenue, String period) {
        logEvent("REVENUE", "Total revenue (" + period + "): $" + String.format("%.2f", revenue));
    }
    
    public void logSlotUsage(String slotName, int usage) {
        logEvent("SLOT_USAGE", "Slot " + slotName + " used " + usage + " times");
    }
    
    public void logSearchQuery(String username, String query) {
        logEvent("SEARCH", "User " + username + " searched for: \"" + query + "\"");
    }
    
    public void logSessionActivity(int userId, int sessionId, String activity) {
        logEvent("SESSION", "Session " + sessionId + " (User: " + userId + ") activity: " + activity);
    }
    
    private synchronized void logEvent(String eventType, String message) {
        String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
        String logEntry = String.format("[%s] [%s] %s%n", timestamp, eventType, message);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(logEntry);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
        
        // Also print to console for immediate visibility
        System.out.println(logEntry.trim());
    }
    
    public void logSeparator() {
        logEvent("SEPARATOR", "===============================================");
    }
    
    public void logCustomEvent(String eventType, String message) {
        logEvent(eventType, message);
    }
    
    public void clearLogs() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, false))) {
            writer.write("");
            logEvent("LOG_CLEAR", "Log file cleared");
        } catch (IOException e) {
            System.err.println("Failed to clear log file: " + e.getMessage());
        }
    }
}
