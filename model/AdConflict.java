package model;

/**
 * Represents an ad conflict detected between two ads.
 * Conflicts are detected automatically based on keyword overlaps and competing brands.
 */
public class AdConflict {
    private int id;
    private int ad1Id;
    private int ad2Id;
    private String reason;
    private java.time.LocalDateTime detectedAt;
    
    public AdConflict(int id, int ad1Id, int ad2Id, String reason) {
        this.id = id;
        this.ad1Id = ad1Id;
        this.ad2Id = ad2Id;
        this.reason = reason;
        this.detectedAt = java.time.LocalDateTime.now();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getAd1Id() {
        return ad1Id;
    }
    
    public void setAd1Id(int ad1Id) {
        this.ad1Id = ad1Id;
    }
    
    public int getAd2Id() {
        return ad2Id;
    }
    
    public void setAd2Id(int ad2Id) {
        this.ad2Id = ad2Id;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public java.time.LocalDateTime getDetectedAt() {
        return detectedAt;
    }
    
    public void setDetectedAt(java.time.LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }
}
