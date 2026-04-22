package engine;

import model.*;
import dsa.heap.MaxHeap;
import dsa.graph.ConflictGraph;
import dsa.slidingwindow.MemoryWindow;
import context.ContextMatcher;
import java.util.*;

public class AllocationEngine {
    private ScoringEngine scoringEngine;
    private ConflictGraph conflictGraph;
    private ContextMatcher contextMatcher;
    private MemoryWindow memoryWindow;
    private Random random;
    
    public AllocationEngine() {
        this.memoryWindow = new MemoryWindow(1000); // Keep last 1000 events
        this.scoringEngine = new ScoringEngine(memoryWindow);
        this.conflictGraph = new ConflictGraph();
        this.contextMatcher = new ContextMatcher();
        this.random = new Random();
    }
    
    public AllocationResult allocateAds(List<Ad> ads, String searchText, String pageContent, 
                                     int userId, int sessionId, Map<Integer, Advertiser> advertisers) {
        
        AllocationResult result = new AllocationResult(userId, sessionId);
        
        // Build context matcher if not already built
        if (ads != null && !ads.isEmpty()) {
            contextMatcher.buildKeywordIndex(ads);
        }
        
        // Calculate context scores for all ads
        Map<Integer, Double> contextScores = contextMatcher.calculateContextScores(searchText, pageContent, ads);
        
        // Process each slot independently
        for (AdSlot slot : AdSlot.values()) {
            Ad allocatedAd = allocateForSlot(ads, slot, contextScores, advertisers);
            if (allocatedAd != null) {
                double contextScore = contextScores.getOrDefault(allocatedAd.getId(), 0.0);
                double finalScore = scoringEngine.calculateScore(allocatedAd, slot, contextScore, userId);
                double finalPrice = calculateFinalPrice(allocatedAd, ads, slot, contextScores, userId);
                
                result.addAllocatedAd(slot, allocatedAd, finalScore, finalPrice);
                
                // Log the allocation event
                AllocationEvent event = new AllocationEvent(userId, sessionId, allocatedAd.getId(), 
                                                          slot, finalScore, finalPrice);
                memoryWindow.addEvent(event);
            }
        }
        
        return result;
    }
    
    private Ad allocateForSlot(List<Ad> ads, AdSlot slot, Map<Integer, Double> contextScores, 
                              Map<Integer, Advertiser> advertisers) {
        
        if (ads == null || ads.isEmpty()) return null;
        
        // Create scored ad entries for heap
        List<ScoredAd> scoredAds = new ArrayList<>();
        
        for (Ad ad : ads) {
            if (!scoringEngine.isAdEligible(ad, slot)) {
                continue;
            }
            
            // Check advertiser budget
            Advertiser advertiser = advertisers.get(ad.getAdvertiserId());
            if (advertiser == null || !advertiser.hasBudget(ad.getBidAmount())) {
                continue;
            }
            
            double contextScore = contextScores.getOrDefault(ad.getId(), 0.0);
            double score = scoringEngine.calculateScore(ad, slot, contextScore, -1);
            
            scoredAds.add(new ScoredAd(ad, score));
        }
        
        if (scoredAds.isEmpty()) return null;
        
        // Use max heap to find best ad
        MaxHeap<ScoredAd> heap = new MaxHeap<>(scoredAds);
        
        // Try ads in order of score, respecting conflicts
        Set<Integer> selectedAdvertisers = new HashSet<>();
        
        while (!heap.isEmpty()) {
            ScoredAd scoredAd = heap.extractMax();
            Ad ad = scoredAd.getAd();
            int advertiserId = ad.getAdvertiserId();
            
            // Check conflicts with already selected advertisers
            if (conflictGraph.canAddAdvertiser(selectedAdvertisers, advertiserId)) {
                // Deduct budget and return ad
                Advertiser advertiser = advertisers.get(advertiserId);
                if (advertiser != null) {
                    advertiser.deductBudget(ad.getBidAmount());
                }
                return ad;
            }
        }
        
        return null;
    }
    
    private double calculateFinalPrice(Ad winnerAd, List<Ad> ads, AdSlot slot, 
                                     Map<Integer, Double> contextScores, int userId) {
        
        // Find second highest score for second-price auction
        double secondHighestScore = 0.0;
        
        for (Ad ad : ads) {
            if (ad.getId() == winnerAd.getId()) continue;
            
            if (!scoringEngine.isAdEligible(ad, slot)) continue;
            
            double contextScore = contextScores.getOrDefault(ad.getId(), 0.0);
            double score = scoringEngine.calculateScore(ad, slot, contextScore, userId);
            
            secondHighestScore = Math.max(secondHighestScore, score);
        }
        
        return scoringEngine.calculateSecondPrice(
            scoringEngine.calculateScore(winnerAd, slot, contextScores.getOrDefault(winnerAd.getId(), 0.0), userId),
            secondHighestScore
        );
    }
    
    public void recordClick(int adId, int userId, int sessionId) {
        // Update memory window with click
        List<AllocationEvent> events = memoryWindow.getRecentEvents(50);
        for (AllocationEvent event : events) {
            if (event.getAdId() == adId && event.getUserId() == userId && 
                event.getSessionId() == sessionId && !event.getWasClicked()) {
                event.markAsClicked();
                break;
            }
        }
    }
    
    public void addConflict(int advertiser1, int advertiser2) {
        conflictGraph.addEdge(advertiser1, advertiser2);
    }
    
    public void removeConflict(int advertiser1, int advertiser2) {
        conflictGraph.removeEdge(advertiser1, advertiser2);
    }
    
    public MemoryWindow getMemoryWindow() {
        return memoryWindow;
    }
    
    public ConflictGraph getConflictGraph() {
        return conflictGraph;
    }
    
    // Helper class for heap ordering
    private static class ScoredAd implements Comparable<ScoredAd> {
        private Ad ad;
        private double score;
        
        public ScoredAd(Ad ad, double score) {
            this.ad = ad;
            this.score = score;
        }
        
        public Ad getAd() { return ad; }
        public double getScore() { return score; }
        
        @Override
        public int compareTo(ScoredAd other) {
            return Double.compare(this.score, other.score);
        }
    }
}
