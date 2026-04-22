package auth;

public class AuthContext {
    private int userId;
    private String username;
    private RoleType role;
    private String sessionToken;
    private boolean authenticated;
    
    public AuthContext() {
        this.authenticated = false;
    }
    
    public AuthContext(int userId, String username, RoleType role, String sessionToken) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.sessionToken = sessionToken;
        this.authenticated = true;
    }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public RoleType getRole() { return role; }
    public void setRole(RoleType role) { this.role = role; }
    
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    
    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
    
    public void logout() {
        this.authenticated = false;
        this.userId = 0;
        this.username = null;
        this.role = null;
        this.sessionToken = null;
    }
}
