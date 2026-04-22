package engine;

import model.Advertiser;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class BudgetPacingEngine {
    private static final double PACING_FACTOR = 0.8; // Use 80% of budget by end of period
    private static final int PACING_PERIOD_HOURS = 24; // 24-hour pacing period
    
    public boolean shouldAllowBid(Advertiser advertiser, double bidAmount) {
        if (advertiser == null || bidAmount <= 0) return false;
        
        return advertiser.hasBudget(bidAmount) && isWithinPacingLimits(advertiser, bidAmount);
    }
    
    private boolean isWithinPacingLimits(Advertiser advertiser, double bidAmount) {
        double remainingBudget = advertiser.getRemainingBudget();
        double totalBudget = advertiser.getTotalBudget();
        
        if (remainingBudget <= 0) return false;
        
        // Calculate expected spend rate
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusHours(PACING_PERIOD_HOURS);
        
        long hoursElapsed = ChronoUnit.HOURS.between(startOfDay, now);
        long totalHours = ChronoUnit.HOURS.between(startOfDay, endOfDay);
        
        if (hoursElapsed <= 0) return true; // Start of day, allow
        
        // Calculate expected budget usage by this time
        double expectedBudgetUsage = (totalBudget * PACING_FACTOR * hoursElapsed) / totalHours;
        double actualBudgetUsage = totalBudget - remainingBudget;
        
        // Allow if we're under the expected usage
        return actualBudgetUsage <= expectedBudgetUsage;
    }
    
    public double getRecommendedBid(Advertiser advertiser, double baseBid) {
        if (advertiser == null || baseBid <= 0) return 0.0;
        
        double remainingBudget = advertiser.getRemainingBudget();
        double totalBudget = advertiser.getTotalBudget();
        
        // Adjust bid based on budget remaining percentage
        double budgetRatio = remainingBudget / totalBudget;
        
        if (budgetRatio > 0.7) {
            return baseBid; // Full bid allowed
        } else if (budgetRatio > 0.4) {
            return baseBid * 0.9; // Reduce by 10%
        } else if (budgetRatio > 0.2) {
            return baseBid * 0.7; // Reduce by 30%
        } else {
            return baseBid * 0.5; // Reduce by 50%
        }
    }
    
    public boolean isBudgetDepleted(Advertiser advertiser) {
        return advertiser.getRemainingBudget() <= 0;
    }
    
    public boolean isBudgetLow(Advertiser advertiser, double threshold) {
        double remainingBudget = advertiser.getRemainingBudget();
        double totalBudget = advertiser.getTotalBudget();
        return remainingBudget <= (totalBudget * threshold);
    }
    
    public void replenishBudget(Advertiser advertiser, double amount) {
        if (advertiser != null && amount > 0) {
            advertiser.setRemainingBudget(advertiser.getRemainingBudget() + amount);
            advertiser.setTotalBudget(advertiser.getTotalBudget() + amount);
        }
    }
    
    public void resetDailyBudgets(Map<Integer, Advertiser> advertisers) {
        // Reset all advertisers to their total budget (daily reset)
        for (Advertiser advertiser : advertisers.values()) {
            advertiser.setRemainingBudget(advertiser.getTotalBudget());
        }
    }
    
    public double getBudgetUtilizationRate(Advertiser advertiser) {
        if (advertiser == null || advertiser.getTotalBudget() <= 0) return 0.0;
        
        double spent = advertiser.getTotalBudget() - advertiser.getRemainingBudget();
        return spent / advertiser.getTotalBudget();
    }
}
