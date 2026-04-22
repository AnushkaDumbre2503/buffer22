package dsa.graph;

import java.util.*;

public class ConflictGraph {
    private Map<Integer, Set<Integer>> adjacencyList;
    
    public ConflictGraph() {
        this.adjacencyList = new HashMap<>();
    }
    
    public void addVertex(int advertiserId) {
        adjacencyList.putIfAbsent(advertiserId, new HashSet<>());
    }
    
    public void addEdge(int advertiser1, int advertiser2) {
        // Add both vertices if they don't exist
        addVertex(advertiser1);
        addVertex(advertiser2);
        
        // Add edges in both directions (undirected graph)
        adjacencyList.get(advertiser1).add(advertiser2);
        adjacencyList.get(advertiser2).add(advertiser1);
    }
    
    public void removeEdge(int advertiser1, int advertiser2) {
        if (adjacencyList.containsKey(advertiser1)) {
            adjacencyList.get(advertiser1).remove(advertiser2);
        }
        if (adjacencyList.containsKey(advertiser2)) {
            adjacencyList.get(advertiser2).remove(advertiser1);
        }
    }
    
    public boolean hasConflict(int advertiser1, int advertiser2) {
        return adjacencyList.containsKey(advertiser1) && 
               adjacencyList.get(advertiser1).contains(advertiser2);
    }
    
    public Set<Integer> getConflicts(int advertiserId) {
        return adjacencyList.getOrDefault(advertiserId, new HashSet<>());
    }
    
    public Set<Integer> getMaximumIndependentSet(Set<Integer> candidateAdvertisers) {
        Set<Integer> mis = new HashSet<>();
        Set<Integer> candidates = new HashSet<>(candidateAdvertisers);
        
        // Greedy approach: pick the vertex with minimum conflicts first
        while (!candidates.isEmpty()) {
            int selected = selectVertexWithMinConflicts(candidates);
            mis.add(selected);
            
            // Remove selected and all its conflicting neighbors
            Set<Integer> toRemove = new HashSet<>();
            toRemove.add(selected);
            toRemove.addAll(getConflicts(selected));
            
            candidates.removeAll(toRemove);
        }
        
        return mis;
    }
    
    private int selectVertexWithMinConflicts(Set<Integer> candidates) {
        int minConflicts = Integer.MAX_VALUE;
        int selected = -1;
        
        for (int candidate : candidates) {
            int conflicts = 0;
            for (int other : candidates) {
                if (candidate != other && hasConflict(candidate, other)) {
                    conflicts++;
                }
            }
            
            if (conflicts < minConflicts) {
                minConflicts = conflicts;
                selected = candidate;
            }
        }
        
        return selected;
    }
    
    public Set<Integer> filterConflictingAds(Set<Integer> selectedAds, int newAdvertiserId) {
        Set<Integer> conflictingAds = new HashSet<>();
        
        if (adjacencyList.containsKey(newAdvertiserId)) {
            for (int adId : selectedAds) {
                if (hasConflict(adId, newAdvertiserId)) {
                    conflictingAds.add(adId);
                }
            }
        }
        
        return conflictingAds;
    }
    
    public boolean canAddAdvertiser(Set<Integer> selectedAds, int newAdvertiserId) {
        for (int adId : selectedAds) {
            if (hasConflict(adId, newAdvertiserId)) {
                return false;
            }
        }
        return true;
    }
    
    public void clear() {
        adjacencyList.clear();
    }
    
    public int getVertexCount() {
        return adjacencyList.size();
    }
    
    public int getEdgeCount() {
        int count = 0;
        for (Set<Integer> neighbors : adjacencyList.values()) {
            count += neighbors.size();
        }
        return count / 2; // Divide by 2 since it's undirected
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConflictGraph:\n");
        for (Map.Entry<Integer, Set<Integer>> entry : adjacencyList.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
