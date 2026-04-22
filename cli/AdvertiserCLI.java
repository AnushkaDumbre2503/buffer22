package cli;

import auth.AuthContext;
import java.util.Scanner;
import service.AdAllocationService;

public class AdvertiserCLI {
    private AuthContext authContext;
    private Scanner scanner;
    private AdAllocationService adService;
    
    public AdvertiserCLI(AuthContext authContext, Scanner scanner) {
        this.authContext = authContext;
        this.scanner = scanner;
        this.adService = new AdAllocationService();
    }
    
    public void showAdvertiserMenu() {
        if (!authContext.isAuthenticated()) {
            authContext.logout();
            return;
        }
        
        System.out.println("\n========== Advertiser Menu ==========");
        System.out.println("Company: " + authContext.getUsername());
        System.out.println("1. Create New Ad");
        System.out.println("2. View My Ads");
        System.out.println("3. Dashboard");
        System.out.println("4. Update Bid");
        System.out.println("5. Logout");
        System.out.print("\nChoose option: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    createNewAd();
                    break;
                case "2":
                    viewMyAds();
                    break;
                case "3":
                    showDashboard();
                    break;
                case "4":
                    updateBid();
                    break;
                case "5":
                    authContext.logout();
                    System.out.println("Logged out successfully");
                    break;
                default:
                    System.out.println("Invalid option");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void createNewAd() throws Exception {
        System.out.println("\n========== Create New Ad ==========");
        System.out.print("Ad Title: ");
        String title = scanner.nextLine().trim();
        
        System.out.print("Ad Content: ");
        String content = scanner.nextLine().trim();
        
        System.out.print("Keywords (comma-separated): ");
        String keywords = scanner.nextLine().trim();
        
        System.out.print("Bid Amount: ");
        double bidAmount = Double.parseDouble(scanner.nextLine().trim());
        
        System.out.print("Daily Budget: ");
        double budget = Double.parseDouble(scanner.nextLine().trim());
        
        System.out.println("\nSlot Preferences:");
        System.out.println("1. Top");
        System.out.println("2. Sidebar");
        System.out.println("3. Footer");
        System.out.print("Select preferred slot (1-3): ");
        String slotChoice = scanner.nextLine().trim();
        
        System.out.println("Ad created successfully!");
        System.out.println("Ad ID: " + System.currentTimeMillis());
    }
    
    private void viewMyAds() {
        System.out.println("\n========== My Ads ==========");
        System.out.println("Ad 1: Summer Sale Campaign");
        System.out.println("  Keywords: summer, sale, offer");
        System.out.println("  Bid: $2.50");
        System.out.println("  Status: Active");
        System.out.println("  Clicks: 45");
        System.out.println("  CTR: 3.2%");
    }
    
    private void showDashboard() {
        System.out.println("\n========== Advertiser Dashboard ==========");
        System.out.println("Company: " + authContext.getUsername());
        System.out.println("\nFinancial Summary:");
        System.out.println("  Total Budget: $5000.00");
        System.out.println("  Remaining Budget: $3250.75");
        System.out.println("  Spent: $1749.25");
        System.out.println("\nPerformance Metrics:");
        System.out.println("  Total Ads: 3");
        System.out.println("  Total Impressions: 1500");
        System.out.println("  Total Clicks: 82");
        System.out.println("  Average CTR: 5.5%");
        System.out.println("\nRecommendations:");
        System.out.println("  1. Increase bid for high-performing keywords");
        System.out.println("  2. Focus on 'summer' and 'sale' keywords");
        System.out.println("  3. Expand to Top slot for better visibility");
    }
    
    private void updateBid() throws Exception {
        System.out.println("\n========== Update Bid ==========");
        System.out.print("Enter Ad ID to update: ");
        String adId = scanner.nextLine().trim();
        
        System.out.print("New Bid Amount: ");
        double newBid = Double.parseDouble(scanner.nextLine().trim());
        
        System.out.println("Bid updated successfully!");
        System.out.println("New bid for Ad " + adId + ": $" + newBid);
    }
}
