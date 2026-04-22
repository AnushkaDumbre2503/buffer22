package cli;

import auth.AuthContext;
import auth.RoleType;
import java.util.Scanner;

public class RoleBasedCLI {
    private AuthContext authContext;
    private Scanner scanner;
    
    public RoleBasedCLI() {
        this.scanner = new Scanner(System.in);
        this.authContext = new AuthContext();
    }
    
    public void start() {
        System.out.println("\n========================================");
        System.out.println("   Intelligent Ad Allocation Engine");
        System.out.println("========================================\n");
        
        while (true) {
            if (!authContext.isAuthenticated()) {
                showAuthenticationMenu();
            } else {
                showRoleBasedMenu();
            }
        }
    }
    
    private void showAuthenticationMenu() {
        System.out.println("\n1. User Login");
        System.out.println("2. User Signup");
        System.out.println("3. Advertiser Login");
        System.out.println("4. Advertiser Signup");
        System.out.println("5. Admin Login");
        System.out.println("6. Exit");
        System.out.print("\nChoose option: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    userLogin();
                    break;
                case "2":
                    userSignup();
                    break;
                case "3":
                    advertiserLogin();
                    break;
                case "4":
                    advertiserSignup();
                    break;
                case "5":
                    adminLogin();
                    break;
                case "6":
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid option");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void showRoleBasedMenu() {
        switch (authContext.getRole()) {
            case USER:
                new UserCLI(authContext, scanner).showUserMenu();
                break;
            case ADVERTISER:
                new AdvertiserCLI(authContext, scanner).showAdvertiserMenu();
                break;
            case ADMIN:
                new AdminCLI(authContext, scanner).showAdminMenu();
                break;
        }
    }
    
    private void userLogin() throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        auth.UserAuthService userAuth = new auth.UserAuthService();
        AuthContext context = userAuth.login(username, password);
        
        if (context != null) {
            this.authContext = context;
            System.out.println("Login successful. Welcome, " + username + "!");
        } else {
            System.out.println("Invalid credentials");
        }
    }
    
    private void userSignup() throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        auth.UserAuthService userAuth = new auth.UserAuthService();
        if (userAuth.signup(username, email, password)) {
            System.out.println("Signup successful. You can now login.");
        } else {
            System.out.println("Signup failed. Username or email may already exist.");
        }
    }
    
    private void advertiserLogin() throws Exception {
        System.out.print("Company Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        auth.AdvertiserAuthService advertiserAuth = new auth.AdvertiserAuthService();
        AuthContext context = advertiserAuth.login(name, password);
        
        if (context != null) {
            this.authContext = context;
            System.out.println("Login successful. Welcome, " + name + "!");
        } else {
            System.out.println("Invalid credentials");
        }
    }
    
    private void advertiserSignup() throws Exception {
        System.out.print("Company Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Initial Budget: ");
        double budget = Double.parseDouble(scanner.nextLine().trim());
        
        auth.AdvertiserAuthService advertiserAuth = new auth.AdvertiserAuthService();
        if (advertiserAuth.signup(name, email, password)) {
            System.out.println("Signup successful. You can now login.");
        } else {
            System.out.println("Signup failed. Company name or email may already exist.");
        }
    }
    
    private void adminLogin() throws Exception {
        System.out.print("Admin Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        auth.AdminAuthService adminAuth = new auth.AdminAuthService();
        AuthContext context = adminAuth.login(username, password);
        
        if (context != null) {
            this.authContext = context;
            System.out.println("Admin login successful. Welcome, " + username + "!");
        } else {
            System.out.println("Invalid admin credentials");
        }
    }
}
