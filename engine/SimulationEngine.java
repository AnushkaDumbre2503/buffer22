package engine;

import model.*;
import dsa.heap.MaxHeap;
import java.util.*;

public class SimulationEngine {
    private AllocationEngine allocationEngine;
    private BudgetPacingEngine budgetPacingEngine;
    private Random random;
    
    public SimulationEngine(AllocationEngine allocationEngine) {
        this.allocationEngine = allocationEngine;
        this.budgetPacingEngine = new BudgetPacingEngine();
        this.random = new Random();
    }
    
    public SimulationResult simulateBidIncrease(Ad ad, double newBidAmount, List<Ad> competitorAds, 
                                              String searchText, String pageContent, int userId, int sessionId,
                                              Map<Integer, Advertiser> advertisers) {
        
        // Store original bid
        double originalBid = ad.getBidAmount();
        
        // Simulate with new bid
        ad.setBidAmount(newBidAmount);
        
        List<Ad> allAds = new ArrayList<>();
        allAds.add(ad);
        allAds.addAll(competitorAds);
        
        AllocationResult result = allocationEngine.allocateAds(allAds, searchText, pageContent, userId, sessionId, advertisers);
        
        // Calculate metrics
        boolean wonTopSlot = result.getAllocatedAds().containsKey(AdSlot.TOP) && 
                            result.getAllocatedAds().get(AdSlot.TOP).getId() == ad.getId();
        
        double estimatedCTR = wonTopSlot ? 0.05 : (result.getAllocatedAds().containsValue(ad) ? 0.02 : 0.0);
        double estimatedCost = result.getFinalPriceForSlot(AdSlot.TOP);
        
        // Restore original bid
        ad.setBidAmount(originalBid);
        
        return new SimulationResult(wonTopSlot, estimatedCTR, estimatedCost, newBidAmount);
    }
    
    public SimulationResult simulateBudgetChange(Advertiser advertiser, double newBudget, 
                                                List<Ad> ads, String searchText, String pageContent,
                                                int userId, int sessionId) {
        
        double originalBudget = advertiser.getRemainingBudget();
        double originalTotal = advertiser.getTotalBudget();
        
        // Simulate with new budget
        advertiser.setRemainingBudget(newBudget);
        advertiser.setTotalBudget(newBudget);
        
        AllocationResult result = allocationEngine.allocateAds(ads, searchText, pageContent, userId, sessionId, 
                                                             Collections.singletonMap(advertiser.getId(), advertiser));
        
        // Calculate metrics
        int allocatedSlots = result.getTotalAdsAllocated();
        double totalCost = result.getTotalRevenue();
        
        // Restore original budget
        advertiser.setRemainingBudget(originalBudget);
        advertiser.setTotalBudget(originalTotal);
        
        return new SimulationResult(allocatedSlots > 0, allocatedSlots * 0.03, totalCost, newBudget);
    }
    
    public List<SimulationResult> simulateKeywordChanges(Ad ad, List<String> newKeywords, 
                                                        String searchText, String pageContent,
                                                        List<Ad> competitorAds, int userId, int sessionId,
                                                        Map<Integer, Advertiser> advertisers) {
        
        List<String> originalKeywords = new ArrayList<>(ad.getKeywords());
        List<SimulationResult> results = new ArrayList<>();
        
        for (String keyword : newKeywords) {
            // Simulate adding this keyword
            ad.getKeywords().add(keyword);
            
            List<Ad> allAds = new ArrayList<>();
            allAds.add(ad);
            allAds.addAll(competitorAds);
            
            AllocationResult result = allocationEngine.allocateAds(allAds, searchText, pageContent, userId, sessionId, advertisers);
            
            boolean won = result.getAllocatedAds().containsValue(ad);
            double score = result.getScores().values().stream().mapToDouble(Double::doubleValue).sum();
            
            results.add(new SimulationResult(won, score, 0.0, keyword));
            
            // Remove the keyword for next iteration
            ad.getKeywords().remove(keyword);
        }
        
        // Restore original keywords
        ad.setKeywords(originalKeywords);
        
        return results;
    }
    
    public PerformanceForecast predictPerformance(Ad ad, int daysToPredict, 
                                                  List<Ad> competitorAds) {
        
        List<Double> predictedCTR = new ArrayList<>();
        List<Double> predictedCost = new ArrayList<>();
        List<Integer> predictedImpressions = new ArrayList<>();
        
        double baseCTR = ad.getCTR();
        double baseBid = ad.getBidAmount();
        
        for (int day = 0; day < daysToPredict; day++) {
            // Simple linear model with some randomness
            double dailyCTR = baseCTR * (1 + (random.nextGaussian() * 0.1));
            dailyCTR = Math.max(0.001, Math.min(0.1, dailyCTR));
            
            int dailyImpressions = 100 + random.nextInt(900); // 100-1000 impressions
            double dailyCost = dailyImpressions * baseBid * 0.8; // Assuming 80% win rate
            
            predictedCTR.add(dailyCTR);
            predictedCost.add(dailyCost);
            predictedImpressions.add(dailyImpressions);
        }
        
        return new PerformanceForecast(predictedCTR, predictedCost, predictedImpressions);
    }
    
    public Map<String, Double> analyzeCompetition(List<Ad> competitorAds, String searchText, String pageContent) {
        Map<String, Double> competitionMetrics = new HashMap<>();
        
        if (competitorAds == null || competitorAds.isEmpty()) {
            competitionMetrics.put("avg_bid", 0.0);
            competitionMetrics.put("max_bid", 0.0);
            competitionMetrics.put("competition_level", 0.0);
            return competitionMetrics;
        }
        
        double totalBid = 0.0;
        double maxBid = 0.0;
        
        for (Ad ad : competitorAds) {
            totalBid += ad.getBidAmount();
            maxBid = Math.max(maxBid, ad.getBidAmount());
        }
        
        double avgBid = totalBid / competitorAds.size();
        double competitionLevel = Math.min(1.0, competitorAds.size() / 10.0); // Normalize to 0-1
        
        competitionMetrics.put("avg_bid", avgBid);
        competitionMetrics.put("max_bid", maxBid);
        competitionMetrics.put("competition_level", competitionLevel);
        
        return competitionMetrics;
    }
    
    // Helper classes for simulation results
    public static class SimulationResult {
        private boolean success;
        private double metric;
        private double cost;
        private Object parameter;
        
        public SimulationResult(boolean success, double metric, double cost, Object parameter) {
            this.success = success;
            this.metric = metric;
            this.cost = cost;
            this.parameter = parameter;
        }
        
        public boolean isSuccess() { return success; }
        public double getMetric() { return metric; }
        public double getCost() { return cost; }
        public Object getParameter() { return parameter; }
        
        @Override
        public String toString() {
            return String.format("SimulationResult{success=%s, metric=%.4f, cost=%.2f, param=%s}", 
                               success, metric, cost, parameter);
        }
    }
    
    public static class PerformanceForecast {
        private List<Double> predictedCTR;
        private List<Double> predictedCost;
        private List<Integer> predictedImpressions;
        
        public PerformanceForecast(List<Double> predictedCTR, List<Double> predictedCost, List<Integer> predictedImpressions) {
            this.predictedCTR = predictedCTR;
            this.predictedCost = predictedCost;
            this.predictedImpressions = predictedImpressions;
        }
        
        public List<Double> getPredictedCTR() { return predictedCTR; }
        public List<Double> getPredictedCost() { return predictedCost; }
        public List<Integer> getPredictedImpressions() { return predictedImpressions; }
        
        public double getAverageCTR() {
            return predictedCTR.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
        
        public double getTotalCost() {
            return predictedCost.stream().mapToDouble(Double::doubleValue).sum();
        }
        
        public int getTotalImpressions() {
            return predictedImpressions.stream().mapToInt(Integer::intValue).sum();
        }
    }
}
