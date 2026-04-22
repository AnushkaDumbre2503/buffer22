package auth;

public enum RoleType {
    USER("User"),
    ADVERTISER("Advertiser"),
    ADMIN("System Officer");
    
    private final String displayName;
    
    RoleType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
