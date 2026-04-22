package cli;

import auth.AuthContext;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import model.Ad;
import model.Advertiser;
import repository.AdRepository;
import repository.AdvertiserRepository;
import repository.AllocationRepository;
import service.AdAllocationService;

public class AdvertiserCLI {
    private AuthContext authContext;
    private Scanner scanner;
    private AdAllocationService adService;
    private AdRepository adRepository;
    private AdvertiserRepository advertiserRepository;
    private AllocationRepository allocationRepository;
    
    public AdvertiserCLI(AuthContext authContext, Scanner scanner) {
        this.authContext = authContext;
        this.scanner = scanner;
        this.adService = new AdAllocationService();
        this.adRepository = new AdRepository();
        this.advertiserRepository = new AdvertiserRepository();
        this.allocationRepository = new AllocationRepository();
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
        try {
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
            
            // Get advertiser ID
            Optional<Advertiser> advertiserOpt = advertiserRepository.findByName(authContext.getUsername());
            if (!advertiserOpt.isPresent()) {
                System.out.println("Error: Advertiser not found");
                return;
            }
            
            int advertiserId = advertiserOpt.get().getId();
            
            // Create and save ad to database
            Ad newAd = new Ad(advertiserId, title, content, bidAmount, keywords);
            Ad savedAd = adRepository.create(newAd);
            
            System.out.println("Ad created successfully!");
            System.out.println("Ad ID: " + savedAd.getId());
            System.out.println("Title: " + savedAd.getTitle());
            System.out.println("Bid Amount: $" + savedAd.getBidAmount());
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid numbers for bid and budget.");
        } catch (SQLException e) {
            System.out.println("Error creating ad: " + e.getMessage());
        }
    }
    
    private void viewMyAds() {
        try {
            System.out.println("\n========== My Ads ==========");
            
            // Get advertiser ID from username
            Optional<Advertiser> advertiserOpt = advertiserRepository.findByName(authContext.getUsername());
            
            if (!advertiserOpt.isPresent()) {
                System.out.println("Error: Advertiser not found");
                return;
            }
            
            int advertiserId = advertiserOpt.get().getId();
            
            // Fetch ads for this advertiser
            List<Ad> myAds = adRepository.findByAdvertiserId(advertiserId);
            
            if (myAds.isEmpty()) {
                System.out.println("No ads found. Create one to get started!");
                return;
            }
            
            // Display each ad with its metrics
            for (int i = 0; i < myAds.size(); i++) {
                Ad ad = myAds.get(i);
                double ctr = ad.getTotalShown() > 0 ? (double) ad.getTotalClicked() / ad.getTotalShown() * 100 : 0.0;
                
                System.out.println("Ad " + (i + 1) + ": " + ad.getTitle());
                System.out.println("  Keywords: " + String.join(", ", ad.getKeywords()));
                System.out.println("  Bid: $" + ad.getBidAmount());
                System.out.println("  Status: Active");
                System.out.println("  Clicks: " + ad.getTotalClicked());
                System.out.printf("  CTR: %.1f%%\n", ctr);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching ads: " + e.getMessage());
        }
    }
    
    private void showDashboard() {
        try {
            System.out.println("\n========== Advertiser Dashboard ==========");
            System.out.println("Company: " + authContext.getUsername());
            
            // Get advertiser by name
            Optional<Advertiser> advertiserOpt = advertiserRepository.findByName(authContext.getUsername());
            
            if (!advertiserOpt.isPresent()) {
                System.out.println("Error: Advertiser not found");
                return;
            }
            
            Advertiser advertiser = advertiserOpt.get();
            int advertiserId = advertiser.getId();
            
            // Get all ads for this advertiser
            List<Ad> myAds = adRepository.findByAdvertiserId(advertiserId);
            
            // Calculate financial metrics
            double totalBudget = advertiser.getTotalBudget();
            double remainingBudget = advertiser.getRemainingBudget();
            double spent = totalBudget - remainingBudget;
            
            System.out.println("\nFinancial Summary:");
            System.out.printf("  Total Budget: $%.2f\n", totalBudget);
            System.out.printf("  Remaining Budget: $%.2f\n", remainingBudget);
            System.out.printf("  Spent: $%.2f\n", spent);
            
            // Calculate performance metrics
            int totalAds = myAds.size();
            int totalImpressions = 0;
            int totalClicks = 0;
            double sumCTR = 0;
            
            for (Ad ad : myAds) {
                totalImpressions += ad.getTotalShown();
                totalClicks += ad.getTotalClicked();
                sumCTR += ad.getCTR();
            }
            
            double averageCTR = totalAds > 0 ? (sumCTR / totalAds) * 100 : 0.0;
            
            System.out.println("\nPerformance Metrics:");
            System.out.println("  Total Ads: " + totalAds);
            System.out.println("  Total Impressions: " + totalImpressions);
            System.out.println("  Total Clicks: " + totalClicks);
            System.out.printf("  Average CTR: %.1f%%\n", averageCTR);
            
            // Generate dynamic recommendations based on performance
            System.out.println("\nRecommendations:");
            generateRecommendations(myAds, remainingBudget, averageCTR);
            
        } catch (SQLException e) {
            System.out.println("Error loading dashboard: " + e.getMessage());
        }
    }
    
    private void generateRecommendations(List<Ad> ads, double remainingBudget, double averageCTR) {
        int recCount = 1;
        
        if (ads.isEmpty()) {
            System.out.println("  " + recCount++ + ". Create your first ad to start advertising");
            return;
        }
        
        // Find high-performing ad
        Ad bestAd = ads.stream()
            .max((a, b) -> Integer.compare(a.getTotalClicked(), b.getTotalClicked()))
            .orElse(null);
        
        if (bestAd != null && bestAd.getTotalClicked() > 0) {
            String topKeywords = String.join("' and '", bestAd.getKeywords());
            System.out.println("  " + recCount++ + ". Increase bid for high-performing keywords: '" + topKeywords + "'");
        }
        
        // Check budget status
        if (remainingBudget < 100) {
            System.out.println("  " + recCount++ + ". Top up your budget - only $" + String.format("%.2f", remainingBudget) + " remaining");
        } else if (averageCTR < 2.0) {
            System.out.println("  " + recCount++ + ". Improve CTR by refining keywords and ad content");
        }
        
        // Low impression ads
        Ad lowPerformer = ads.stream()
            .filter(a -> a.getTotalShown() < 50)
            .findFirst()
            .orElse(null);
        
        if (lowPerformer != null) {
            System.out.println("  " + recCount++ + ". Expand to Top slot for better visibility on '" + lowPerformer.getTitle() + "'");
        }
    }
    
    private void updateBid() throws Exception {
        try {
            System.out.println("\n========== Update Bid ==========");
            
            // Get advertiser ID
            Optional<Advertiser> advertiserOpt = advertiserRepository.findByName(authContext.getUsername());
            if (!advertiserOpt.isPresent()) {
                System.out.println("Error: Advertiser not found");
                return;
            }
            
            int advertiserId = advertiserOpt.get().getId();
            
            // Get all ads for this advertiser
            List<Ad> myAds = adRepository.findByAdvertiserId(advertiserId);
            
            if (myAds.isEmpty()) {
                System.out.println("No ads found to update");
                return;
            }
            
            // Show available ads
            System.out.println("Your Ads:");
            for (int i = 0; i < myAds.size(); i++) {
                System.out.println("  " + (i + 1) + ". ID: " + myAds.get(i).getId() + " - " + myAds.get(i).getTitle() + " (Current bid: $" + myAds.get(i).getBidAmount() + ")");
            }
            
            System.out.print("Enter Ad ID to update: ");
            int adId = Integer.parseInt(scanner.nextLine().trim());
            
            // Find the ad
            Optional<Ad> adOpt = adRepository.findById(adId);
            if (!adOpt.isPresent() || adOpt.get().getAdvertiserId() != advertiserId) {
                System.out.println("Ad not found or you don't have permission to update it");
                return;
            }
            
            System.out.print("New Bid Amount: ");
            double newBid = Double.parseDouble(scanner.nextLine().trim());
            
            // Update bid in database
            if (adRepository.updateBid(adId, newBid)) {
                System.out.println("Bid updated successfully!");
                System.out.println("New bid for Ad " + adId + ": $" + newBid);
            } else {
                System.out.println("Failed to update bid");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid numbers.");
        } catch (SQLException e) {
            System.out.println("Error updating bid: " + e.getMessage());
        }
    }
}
