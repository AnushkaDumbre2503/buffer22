package manager;

import model.AdSlot;
import java.util.HashMap;
import java.util.Map;

public class SlotManager {
    private Map<AdSlot, Integer> slotUsage;
    private Map<AdSlot, Map<Integer, Long>> lastShownTimes;
    
    public SlotManager() {
        this.slotUsage = new HashMap<>();
        this.lastShownTimes = new HashMap<>();
        
        // Initialize slot usage counters
        for (AdSlot slot : AdSlot.values()) {
            slotUsage.put(slot, 0);
            lastShownTimes.put(slot, new HashMap<>());
        }
    }
    
    public boolean isAdInCooldown(int adId, AdSlot slot) {
        Map<Integer, Long> slotLastShown = lastShownTimes.get(slot);
        if (slotLastShown == null) return false;
        
        Long lastShown = slotLastShown.get(adId);
        if (lastShown == null) return false;
        
        long currentTime = System.currentTimeMillis();
        long cooldownMs = slot.getCooldownSeconds() * 1000L;
        
        return (currentTime - lastShown) < cooldownMs;
    }
    
    public void recordAdShown(int adId, AdSlot slot) {
        // Increment slot usage
        slotUsage.put(slot, slotUsage.get(slot) + 1);
        
        // Record last shown time
        Map<Integer, Long> slotLastShown = lastShownTimes.get(slot);
        if (slotLastShown != null) {
            slotLastShown.put(adId, System.currentTimeMillis());
        }
    }
    
    public int getSlotUsage(AdSlot slot) {
        return slotUsage.getOrDefault(slot, 0);
    }
    
    public Map<AdSlot, Integer> getAllSlotUsage() {
        return new HashMap<>(slotUsage);
    }
    
    public Map<AdSlot, Double> getSlotPerformance() {
        Map<AdSlot, Double> performance = new HashMap<>();
        int totalUsage = slotUsage.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalUsage == 0) {
            for (AdSlot slot : AdSlot.values()) {
                performance.put(slot, 0.0);
            }
            return performance;
        }
        
        for (Map.Entry<AdSlot, Integer> entry : slotUsage.entrySet()) {
            double percentage = (double) entry.getValue() / totalUsage * 100;
            performance.put(entry.getKey(), percentage);
        }
        
        return performance;
    }
    
    public AdSlot getMostUsedSlot() {
        AdSlot mostUsed = null;
        int maxUsage = -1;
        
        for (Map.Entry<AdSlot, Integer> entry : slotUsage.entrySet()) {
            if (entry.getValue() > maxUsage) {
                maxUsage = entry.getValue();
                mostUsed = entry.getKey();
            }
        }
        
        return mostUsed;
    }
    
    public AdSlot getLeastUsedSlot() {
        AdSlot leastUsed = null;
        int minUsage = Integer.MAX_VALUE;
        
        for (Map.Entry<AdSlot, Integer> entry : slotUsage.entrySet()) {
            if (entry.getValue() < minUsage) {
                minUsage = entry.getValue();
                leastUsed = entry.getKey();
            }
        }
        
        return leastUsed;
    }
    
    public void clearSlotUsage() {
        slotUsage.clear();
        for (AdSlot slot : AdSlot.values()) {
            slotUsage.put(slot, 0);
        }
    }
    
    public void clearCooldowns() {
        lastShownTimes.clear();
        for (AdSlot slot : AdSlot.values()) {
            lastShownTimes.put(slot, new HashMap<>());
        }
    }
    
    public long getTimeUntilAvailable(int adId, AdSlot slot) {
        Map<Integer, Long> slotLastShown = lastShownTimes.get(slot);
        if (slotLastShown == null) return 0;
        
        Long lastShown = slotLastShown.get(adId);
        if (lastShown == null) return 0;
        
        long currentTime = System.currentTimeMillis();
        long cooldownMs = slot.getCooldownSeconds() * 1000L;
        long timeSinceShown = currentTime - lastShown;
        
        return Math.max(0, cooldownMs - timeSinceShown);
    }
    
    public Map<Integer, Long> getAdsInCooldown(AdSlot slot) {
        Map<Integer, Long> cooldownAds = new HashMap<>();
        Map<Integer, Long> slotLastShown = lastShownTimes.get(slot);
        
        if (slotLastShown == null) return cooldownAds;
        
        long currentTime = System.currentTimeMillis();
        long cooldownMs = slot.getCooldownSeconds() * 1000L;
        
        for (Map.Entry<Integer, Long> entry : slotLastShown.entrySet()) {
            int adId = entry.getKey();
            long lastShown = entry.getValue();
            
            if ((currentTime - lastShown) < cooldownMs) {
                long remainingTime = cooldownMs - (currentTime - lastShown);
                cooldownAds.put(adId, remainingTime);
            }
        }
        
        return cooldownAds;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SlotManager:\n");
        
        for (Map.Entry<AdSlot, Integer> entry : slotUsage.entrySet()) {
            sb.append("  ").append(entry.getKey().getName())
              .append(": ").append(entry.getValue()).append(" uses\n");
        }
        
        return sb.toString();
    }
}
