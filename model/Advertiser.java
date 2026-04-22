package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Advertiser {
    private int id;
    private String name;
    private double totalBudget;
    private double remainingBudget;
    private LocalDateTime createdAt;
    private List<Integer> conflictingAdvertisers;
    
    public Advertiser() {
        this.conflictingAdvertisers = new ArrayList<>();
    }
    
    public Advertiser(int id, String name, double totalBudget, double remainingBudget) {
        this.id = id;
        this.name = name;
        this.totalBudget = totalBudget;
        this.remainingBudget = remainingBudget;
        this.createdAt = LocalDateTime.now();
        this.conflictingAdvertisers = new ArrayList<>();
    }
    
    public Advertiser(String name, double totalBudget, double remainingBudget) {
        this.name = name;
        this.totalBudget = totalBudget;
        this.remainingBudget = remainingBudget;
        this.createdAt = LocalDateTime.now();
        this.conflictingAdvertisers = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getTotalBudget() { return totalBudget; }
    public void setTotalBudget(double totalBudget) { this.totalBudget = totalBudget; }
    
    public double getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(double remainingBudget) { this.remainingBudget = remainingBudget; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<Integer> getConflictingAdvertisers() { return conflictingAdvertisers; }
    public void setConflictingAdvertisers(List<Integer> conflictingAdvertisers) { 
        this.conflictingAdvertisers = conflictingAdvertisers; 
    }
    
    public void addConflictingAdvertiser(int advertiserId) {
        if (!conflictingAdvertisers.contains(advertiserId)) {
            conflictingAdvertisers.add(advertiserId);
        }
    }
    
    public boolean hasBudget(double amount) {
        return remainingBudget >= amount;
    }
    
    public void deductBudget(double amount) {
        this.remainingBudget -= amount;
    }
    
    @Override
    public String toString() {
        return "Advertiser{id=" + id + ", name='" + name + "', remainingBudget=" + remainingBudget + "}";
    }
}
