package cli;

import auth.AuthContext;
import java.util.Scanner;
import service.AdAllocationService;

public class UserCLI {
    private AuthContext authContext;
    private Scanner scanner;
    private AdAllocationService adService;
    
    public UserCLI(AuthContext authContext, Scanner scanner) {
        this.authContext = authContext;
        this.scanner = scanner;
        this.adService = new AdAllocationService();
    }
    
    public void showUserMenu() {
        if (!authContext.isAuthenticated()) {
            authContext.logout();
            return;
        }
        
        System.out.println("\n========== User Menu ==========");
        System.out.println("Logged in as: " + authContext.getUsername());
        System.out.println("1. Search & Get Ads");
        System.out.println("2. Click on Ad");
        System.out.println("3. Logout");
        System.out.print("\nChoose option: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    searchAndGetAds();
                    break;
                case "2":
                    clickOnAd();
                    break;
                case "3":
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
    
    private void searchAndGetAds() throws Exception {
        System.out.print("Enter search query: ");
        String query = scanner.nextLine().trim();
        
        System.out.println("\nAvailable Slots:");
        System.out.println("1. Top");
        System.out.println("2. Sidebar");
        System.out.println("3. Footer");
        System.out.print("Select slot (1-3): ");
        
        String slotChoice = scanner.nextLine().trim();
        String slotType = "";
        
        switch (slotChoice) {
            case "1":
                slotType = "TOP";
                break;
            case "2":
                slotType = "SIDEBAR";
                break;
            case "3":
                slotType = "FOOTER";
                break;
            default:
                System.out.println("Invalid slot selection");
                return;
        }
        
        System.out.println("\nFetching content for: " + query);
        String content = ContentFetcher.fetchContent(query);
        
        System.out.println("\n========== ADS ALLOCATED ==========");
        System.out.println("Query: " + query);
        System.out.println("Slot: " + slotType);
        System.out.println("Content Keywords: " + content);
        System.out.println("====================================\n");
    }
    
    private void clickOnAd() {
        System.out.println("Click registered for selected ad");
        System.out.println("Thank you for your interest!");
    }
}
