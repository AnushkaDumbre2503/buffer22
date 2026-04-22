package engine;

import dsa.graph.ConflictGraph;
import model.Ad;
import java.util.*;

public class ConflictResolver {
    private ConflictGraph conflictGraph;
    
    public ConflictResolver() {
        this.conflictGraph = new ConflictGraph();
    }
    
    public ConflictResolver(ConflictGraph conflictGraph) {
        this.conflictGraph = conflictGraph;
    }
    
    public List<Ad> resolveConflicts(List<Ad> candidateAds) {
        if (candidateAds == null || candidateAds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Group ads by advertiser
        Map<Integer, List<Ad>> adsByAdvertiser = new HashMap<>();
        for (Ad ad : candidateAds) {
            adsByAdvertiser.computeIfAbsent(ad.getAdvertiserId(), k -> new ArrayList<>()).add(ad);
        }
        
        // Get maximum independent set of advertisers
        Set<Integer> advertiserIds = new HashSet<>(adsByAdvertiser.keySet());
        Set<Integer> selectedAdvertisers = conflictGraph.getMaximumIndependentSet(advertiserIds);
        
        // Return ads from selected advertisers
        List<Ad> result = new ArrayList<>();
        for (Ad ad : candidateAds) {
            if (selectedAdvertisers.contains(ad.getAdvertiserId())) {
                result.add(ad);
            }
        }
        
        return result;
    }
    
    public boolean canShowTogether(Ad ad1, Ad ad2) {
        if (ad1 == null || ad2 == null) return true;
        return !conflictGraph.hasConflict(ad1.getAdvertiserId(), ad2.getAdvertiserId());
    }
    
    public Set<Integer> getConflictingAdvertisers(int advertiserId) {
        return conflictGraph.getConflicts(advertiserId);
    }
    
    public List<Ad> filterConflictingAds(List<Ad> ads, Ad newAd) {
        if (newAd == null) return ads;
        
        List<Ad> filtered = new ArrayList<>();
        for (Ad ad : ads) {
            if (canShowTogether(ad, newAd)) {
                filtered.add(ad);
            }
        }
        return filtered;
    }
    
    public void addConflict(int advertiser1, int advertiser2) {
        conflictGraph.addEdge(advertiser1, advertiser2);
    }
    
    public void removeConflict(int advertiser1, int advertiser2) {
        conflictGraph.removeEdge(advertiser1, advertiser2);
    }
    
    public void clearConflicts() {
        conflictGraph.clear();
    }
    
    public Map<Integer, Set<Integer>> getAllConflicts() {
        Map<Integer, Set<Integer>> allConflicts = new HashMap<>();
        for (int advertiserId : conflictGraph.getConflicts(-1)) {
            allConflicts.put(advertiserId, conflictGraph.getConflicts(advertiserId));
        }
        return allConflicts;
    }
    
    public boolean hasAnyConflicts(int advertiserId) {
        return !conflictGraph.getConflicts(advertiserId).isEmpty();
    }
    
    public int getConflictCount(int advertiserId) {
        return conflictGraph.getConflicts(advertiserId).size();
    }
}
