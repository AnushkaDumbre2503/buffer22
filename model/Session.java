package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Session {
    private int id;
    private int userId;
    private String sessionToken;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isActive;
    
    public Session() {
        this.sessionToken = UUID.randomUUID().toString();
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusHours(24); // 24 hour session
    }
    
    public Session(int userId) {
        this();
        this.userId = userId;
    }
    
    public Session(int userId, String sessionToken) {
        this.userId = userId;
        this.sessionToken = sessionToken;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusHours(24);
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    // Utility methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void extendSession(int hours) {
        this.expiresAt = LocalDateTime.now().plusHours(hours);
    }
    
    public void invalidate() {
        this.isActive = false;
    }
    
    @Override
    public String toString() {
        return "Session{id=" + id + ", userId=" + userId + ", token=" + sessionToken.substring(0, 8) + "..."
             + ", active=" + isActive + ", expires=" + expiresAt + "}";
    }
}
