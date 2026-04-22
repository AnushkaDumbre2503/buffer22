package cli;

import model.*;
import service.AdAllocationService;
import engine.SimulationEngine;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class CommandHandler {
    private AdAllocationService service;
    private Scanner scanner;
    private User currentUser;
    
    public CommandHandler() {
        this.service = new AdAllocationService();
        this.scanner = new Scanner(System.in);
        this.currentUser = null;
    }
    
    public void start() {
        try {
            service.initializeSystem();
            System.out.println("🎯 Intelligent Ad Allocation Engine - CLI Interface");
            System.out.println("================================================");
            
            while (true) {
                if (currentUser == null) {
                    showMainMenu();
                } else {
                    showUserMenu();
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
        }
    }
    
    private void showMainMenu() {
        System.out.println("\n📋 Main Menu:");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Global System View");
        System.out.println("4. Advertiser Mode");
        System.out.println("5. Exit");
        System.out.print("Choose option: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegister();
                    break;
                case "3":
                    handleGlobalView();
                    break;
                case "4":
                    handleAdvertiserMode();
                    break;
                case "5":
                    System.out.println("👋 Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("❌ Invalid option. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
    
    private void showUserMenu() {
        System.out.println("\n👤 User Menu (Logged in as: " + currentUser.getUsername() + ")");
        System.out.println("1. Search & Get Ads");
        System.out.println("2. Click on Ad");
        System.out.println("3. View My Ad History");
        System.out.println("4. Logout");
        System.out.print("Choose option: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    handleSearchAndAllocate();
                    break;
                case "2":
                    handleClick();
                    break;
                case "3":
                    viewUserHistory();
                    break;
                case "4":
                    currentUser = null;
                    System.out.println("✅ Logged out successfully.");
                    break;
                default:
                    System.out.println("❌ Invalid option. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
    
    private void handleLogin() throws SQLException {
        System.out.print("📧 Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("📧 Email: ");
        String email = scanner.nextLine().trim();
        
        User user = service.login(username, email);
        if (user != null) {
            currentUser = user;
            System.out.println("✅ Login successful! Welcome, " + user.getUsername() + "!");
        } else {
            System.out.println("❌ Login failed. Invalid credentials.");
        }
    }
    
    private void handleRegister() throws SQLException {
        System.out.print("📧 Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("📧 Email: ");
        String email = scanner.nextLine().trim();
        
        try {
            User user = service.registerUser(username, email);
            System.out.println("✅ Registration successful! Your user ID is: " + user.getId());
            System.out.println("💡 You can now login with your credentials.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
        }
    }
    
    private void handleSearchAndAllocate() throws SQLException {
        System.out.print("🔍 Search query: ");
        String searchText = scanner.nextLine().trim();
        System.out.print("📄 Page content (optional): ");
        String pageContent = scanner.nextLine().trim();
        
        AllocationResult result = service.allocateAds(currentUser.getUsername(), searchText, pageContent);
        
        System.out.println("\n🎯 Allocated Ads:");
        System.out.println(result.toString());
    }
    
    private void handleClick() throws SQLException {
        System.out.print("🎯 Enter Ad ID to click: ");
        String adIdStr = scanner.nextLine().trim();
        
        try {
            int adId = Integer.parseInt(adIdStr);
            service.recordClick(currentUser.getUsername(), adId);
            System.out.println("✅ Click recorded successfully!");
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid Ad ID. Please enter a number.");
        }
    }
    
    private void viewUserHistory() throws SQLException {
        List<AllocationEvent> history = service.getUserAllocationHistory(currentUser.getUsername());
        
        if (history.isEmpty()) {
            System.out.println("📭 No ad history found.");
            return;
        }
        
        System.out.println("\n📊 Your Ad History:");
        System.out.println("===============================================");
        for (AllocationEvent event : history) {
            System.out.println(event.toString());
        }
    }
    
    private void handleGlobalView() throws SQLException {
        System.out.println("\n🌍 Global System View");
        System.out.println("=====================");
        
        // System stats
        List<User> users = service.getAllUsers();
        List<Advertiser> advertisers = service.getAllAdvertisers();
        List<Ad> ads = service.getAllAds();
        double totalRevenue = service.getTotalRevenue();
        
        System.out.println("👥 Total Users: " + users.size());
        System.out.println("🏢 Total Advertisers: " + advertisers.size());
        System.out.println("📢 Total Ads: " + ads.size());
        System.out.println("💰 Total Revenue: $" + String.format("%.2f", totalRevenue));
        
        // Slot performance
        System.out.println("\n📊 Slot Performance:");
        var slotPerformance = service.getSlotPerformance();
        for (var entry : slotPerformance.entrySet()) {
            System.out.println("  " + entry.getKey().getName() + ": " + 
                             String.format("%.1f%%", entry.getValue()));
        }
        
        // Recent activity
        System.out.println("\n⏰ Recent Activity (last 10 minutes):");
        List<AllocationEvent> recent = service.getRecentAllocations(10);
        if (recent.isEmpty()) {
            System.out.println("  No recent activity.");
        } else {
            for (AllocationEvent event : recent.subList(0, Math.min(5, recent.size()))) {
                System.out.println("  " + event.toString());
            }
        }
        
        // Popular searches
        System.out.println("\n🔥 Popular Search Terms:");
        List<String> popularSearches = service.getPopularSearchTerms(5);
        if (popularSearches.isEmpty()) {
            System.out.println("  No search data available.");
        } else {
            for (int i = 0; i < popularSearches.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + popularSearches.get(i));
            }
        }
    }
    
    private void handleAdvertiserMode() throws SQLException {
        System.out.println("\n💼 Advertiser Mode");
        System.out.println("==================");
        System.out.println("1. View All Advertisers");
        System.out.println("2. Create New Advertiser");
        System.out.println("3. View All Ads");
        System.out.println("4. Create New Ad");
        System.out.println("5. Simulate Bid Changes");
        System.out.println("6. Add Advertiser Conflict");
        System.out.println("7. Back to Main Menu");
        System.out.print("Choose option: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    viewAllAdvertisers();
                    break;
                case "2":
                    createAdvertiser();
                    break;
                case "3":
                    viewAllAds();
                    break;
                case "4":
                    createAd();
                    break;
                case "5":
                    simulateBidChanges();
                    break;
                case "6":
                    addConflict();
                    break;
                case "7":
                    return;
                default:
                    System.out.println("❌ Invalid option. Please try again.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }
    
    private void viewAllAdvertisers() throws SQLException {
        List<Advertiser> advertisers = service.getAllAdvertisers();
        
        System.out.println("\n🏢 All Advertisers:");
        System.out.println("===============================================");
        for (Advertiser advertiser : advertisers) {
            System.out.println(advertiser.toString());
            if (!advertiser.getConflictingAdvertisers().isEmpty()) {
                System.out.println("  Conflicts with: " + advertiser.getConflictingAdvertisers());
            }
        }
    }
    
    private void createAdvertiser() throws SQLException {
        System.out.print("🏢 Advertiser Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("💰 Budget: ");
        String budgetStr = scanner.nextLine().trim();
        
        try {
            double budget = Double.parseDouble(budgetStr);
            Advertiser advertiser = service.createAdvertiser(name, budget);
            System.out.println("✅ Advertiser created successfully! ID: " + advertiser.getId());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid budget amount.");
        }
    }
    
    private void viewAllAds() throws SQLException {
        List<Ad> ads = service.getAllAds();
        
        System.out.println("\n📢 All Ads:");
        System.out.println("===============================================");
        for (Ad ad : ads) {
            System.out.println(ad.toString());
            System.out.println("  Keywords: " + ad.getKeywords());
        }
    }
    
    private void createAd() throws SQLException {
        System.out.print("🏢 Advertiser ID: ");
        String advertiserIdStr = scanner.nextLine().trim();
        System.out.print("📢 Ad Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("📝 Ad Content: ");
        String content = scanner.nextLine().trim();
        System.out.print("💰 Bid Amount: ");
        String bidStr = scanner.nextLine().trim();
        System.out.print("🏷️  Keywords (comma-separated): ");
        String keywords = scanner.nextLine().trim();
        
        try {
            int advertiserId = Integer.parseInt(advertiserIdStr);
            double bidAmount = Double.parseDouble(bidStr);
            
            Ad ad = service.createAd(advertiserId, title, content, bidAmount, keywords);
            System.out.println("✅ Ad created successfully! ID: " + ad.getId());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid advertiser ID or bid amount.");
        }
    }
    
    private void simulateBidChanges() throws SQLException {
        System.out.println("🎮 Bid Simulation - Coming Soon!");
        System.out.println("This feature would allow advertisers to test different bid strategies.");
    }
    
    private void addConflict() throws SQLException {
        System.out.print("🏢 Advertiser 1 ID: ");
        String adv1Str = scanner.nextLine().trim();
        System.out.print("🏢 Advertiser 2 ID: ");
        String adv2Str = scanner.nextLine().trim();
        
        try {
            int adv1Id = Integer.parseInt(adv1Str);
            int adv2Id = Integer.parseInt(adv2Str);
            
            service.addAdvertiserConflict(adv1Id, adv2Id);
            System.out.println("✅ Conflict added successfully!");
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid advertiser IDs.");
        }
    }
}
