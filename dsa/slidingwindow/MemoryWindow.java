package dsa.slidingwindow;

import model.AllocationEvent;
import java.time.LocalDateTime;
import java.util.*;

public class MemoryWindow {
    private final int maxSize;
    private final LinkedList<AllocationEvent> events;
    
    public MemoryWindow(int maxSize) {
        this.maxSize = maxSize;
        this.events = new LinkedList<>();
    }
    
    public void addEvent(AllocationEvent event) {
        events.addLast(event);
        
        // Remove oldest events if we exceed max size
        while (events.size() > maxSize) {
            events.removeFirst();
        }
    }
    
    public List<AllocationEvent> getRecentEvents(int count) {
        int size = events.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(events.subList(fromIndex, size));
    }
    
    public List<AllocationEvent> getEventsInLastMinutes(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        List<AllocationEvent> recentEvents = new ArrayList<>();
        
        for (AllocationEvent event : events) {
            if (event.getCreatedAt().isAfter(cutoff)) {
                recentEvents.add(event);
            }
        }
        
        return recentEvents;
    }
    
    public int getAdShowCount(int adId, int minutesBack) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutesBack);
        int count = 0;
        
        for (AllocationEvent event : events) {
            if (event.getAdId() == adId && event.getCreatedAt().isAfter(cutoff)) {
                count++;
            }
        }
        
        return count;
    }
    
    public double getAdCTR(int adId, int minutesBack) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutesBack);
        int shown = 0;
        int clicked = 0;
        
        for (AllocationEvent event : events) {
            if (event.getAdId() == adId && event.getCreatedAt().isAfter(cutoff)) {
                shown++;
                if (event.getWasClicked()) {
                    clicked++;
                }
            }
        }
        
        return shown > 0 ? (double) clicked / shown : 0.0;
    }
    
    public boolean isAdInCooldown(int adId, int cooldownMinutes) {
        for (int i = events.size() - 1; i >= 0; i--) {
            AllocationEvent event = events.get(i);
            if (event.getAdId() == adId) {
                LocalDateTime cooldownEnd = event.getCreatedAt().plusMinutes(cooldownMinutes);
                return LocalDateTime.now().isBefore(cooldownEnd);
            }
        }
        return false;
    }
    
    public Map<Integer, Integer> getAdvertiserFrequency(int minutesBack) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutesBack);
        Map<Integer, Integer> frequency = new HashMap<>();
        
        for (AllocationEvent event : events) {
            if (event.getCreatedAt().isAfter(cutoff)) {
                // Note: This would need adId to advertiserId mapping
                // For now, we'll use adId as a proxy
                frequency.put(event.getAdId(), frequency.getOrDefault(event.getAdId(), 0) + 1);
            }
        }
        
        return frequency;
    }
    
    public double getFatigueScore(int adId, int minutesBack) {
        int recentShows = getAdShowCount(adId, minutesBack);
        
        // Exponential fatigue: more recent shows = higher fatigue
        if (recentShows == 0) return 1.0; // No fatigue
        
        // Fatigue increases exponentially with recent shows
        return Math.max(0.1, 1.0 / (1.0 + recentShows * 0.5));
    }
    
    public double getMemoryBoost(int adId, int minutesBack) {
        double ctr = getAdCTR(adId, minutesBack);
        
        // Memory boost based on historical performance
        if (ctr == 0) return 1.0; // Neutral boost
        
        // Higher CTR = higher boost, but capped
        return Math.min(2.0, 1.0 + ctr * 5);
    }
    
    public void clear() {
        events.clear();
    }
    
    public int size() {
        return events.size();
    }
    
    public boolean isEmpty() {
        return events.isEmpty();
    }
    
    public List<AllocationEvent> getAllEvents() {
        return new ArrayList<>(events);
    }
    
    @Override
    public String toString() {
        return "MemoryWindow{size=" + events.size() + "/" + maxSize + "}";
    }
}
