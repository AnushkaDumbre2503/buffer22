package cli;

import auth.AuthContext;
import java.sql.SQLException;
import java.util.*;
import model.*;
import repository.SearchHistoryRepository;
import service.AdminService;
import service.MarkovAnalyticsService;

public class AdminCLI {
    private AuthContext authContext;
    private Scanner scanner;
    private AdminService adminService;
    private MarkovAnalyticsService markovAnalyticsService;
    
    public AdminCLI(AuthContext authContext, Scanner scanner) {
        this.authContext = authContext;
        this.scanner = scanner;
        this.adminService = new AdminService();
        this.markovAnalyticsService = new MarkovAnalyticsService();
    }
    
    public void showAdminMenu() {
        if (!authContext.isAuthenticated()) {
            authContext.logout();
            return;
        }
        
        System.out.println("\n========== System Officer Dashboard ==========");
        System.out.println("Admin: " + authContext.getUsername());
        System.out.println("1. View All Users");
        System.out.println("2. View All Advertisers");
        System.out.println("3. View All Ads");
        System.out.println("4. View Ad Conflicts");
        System.out.println("5. View Slot-wise Allocation");
        System.out.println("6. View Recent Activities");
        System.out.println("7. View Popular Search Terms");
        System.out.println("8. System Statistics");
        System.out.println("9. Markov Analytics Dashboard");
        System.out.println("10. Logout");
        System.out.print("\nChoose option: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    viewAllUsers();
                    break;
                case "2":
                    viewAllAdvertisers();
                    break;
                case "3":
                    viewAllAds();
                    break;
                case "4":
                    viewAdConflicts();
                    break;
                case "5":
                    viewSlotAllocation();
                    break;
                case "6":
                    viewRecentActivities();
                    break;
                case "7":
                    viewPopularSearches();
                    break;
                case "8":
                    viewSystemStatistics();
                    break;
                case "9":
                    viewMarkovAnalytics();
                    break;
                case "10":
                    authContext.logout();
                    System.out.println("Logged out successfully");
                    break;
                default:
                    System.out.println("Invalid option");
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void viewAllUsers() throws SQLException {
        List<User> users = adminService.getAllUsers();
        
        System.out.println("\n========== All Users (Real-time) ==========");
        System.out.println("User ID | Username       | Email                  | Joined");
        System.out.println("--------|----------------|------------------------|----------");
        
        for (User user : users) {
            System.out.printf("%d | %-14s | %-22s | %s%n", 
                user.getId(), 
                user.getUsername(), 
                user.getEmail(),
                user.getCreatedAt().toLocalDate()
            );
        }
        
        System.out.println("\nTotal Users: " + users.size());
    }
    
    private void viewAllAdvertisers() throws SQLException {
        List<Advertiser> advertisers = adminService.getAllAdvertisers();
        
        System.out.println("\n========== All Advertisers (Real-time) ==========");
        System.out.println("ID | Company Name        | Total Budget | Spent  | Remaining | Joined");
        System.out.println("---|---------------------|--------------|--------|-----------|----------");
        
        for (Advertiser adv : advertisers) {
            double spent = adv.getTotalBudget() - adv.getRemainingBudget();
            System.out.printf("%d | %-19s | $%-11.2f | $%-5.2f | $%-8.2f | %s%n",
                adv.getId(),
                adv.getName(),
                adv.getTotalBudget(),
                spent,
                adv.getRemainingBudget(),
                adv.getCreatedAt().toLocalDate()
            );
        }
        
        System.out.println("\nTotal Advertisers: " + advertisers.size());
    }
    
    private void viewAllAds() throws SQLException {
        List<Ad> ads = adminService.getAllAds();
        
        System.out.println("\n========== All Ads (Real-time) ==========");
        System.out.println("Ad ID | Title           | Advertiser ID | Bid Amount | Keywords");
        System.out.println("------|-----------------|---------------|------------|------------------");
        
        for (Ad ad : ads) {
            String keywords = String.join(",", ad.getKeywords());
            System.out.printf("%d | %-15s | %d | $%-9.2f | %s%n",
                ad.getId(),
                ad.getTitle(),
                ad.getAdvertiserId(),
                ad.getBidAmount(),
                keywords
            );
        }
        
        System.out.println("\nTotal Ads: " + ads.size());
    }
    
    private void viewAdConflicts() throws SQLException {
        List<AdConflict> conflicts = adminService.getAdConflicts();
        
        System.out.println("\n========== Auto-Detected Ad Conflicts (Real-time) ==========");
        System.out.println("Conflict ID | Ad 1 ID | Ad 2 ID | Reason");
        System.out.println("------------|---------|---------|---------------------");
        
        for (AdConflict conflict : conflicts) {
            System.out.printf("%d | %d | %d | %s%n",
                conflict.getId(),
                conflict.getAd1Id(),
                conflict.getAd2Id(),
                conflict.getReason()
            );
        }
        
        System.out.println("\nTotal Conflicts: " + conflicts.size());
        System.out.println("Note: Conflicts are auto-detected and cannot be manually created.");
    }
    
    private void viewSlotAllocation() throws SQLException {
        Map<AdSlot, List<AdminService.AllocationRecord>> allocation = adminService.getSlotWiseAllocation();
        
        System.out.println("\n========== Slot-wise Ad Allocation (Real-time) ==========");
        
        for (AdSlot slotType : AdSlot.values()) {
            List<AdminService.AllocationRecord> records = allocation.get(slotType);
            System.out.println("\n" + slotType.getName() + " Slot:");
            
            if (records.isEmpty()) {
                System.out.println("  No allocations for this slot");
            } else {
                for (AdminService.AllocationRecord record : records) {
                    System.out.printf("  - %s (Impressions: %d, Clicks: %d)%n",
                        record.adTitle,
                        record.impressions,
                        record.clicks
                    );
                }
            }
        }
    }
    
    private void viewRecentActivities() throws SQLException {
        List<AdminService.ActivityLog> activities = adminService.getRecentActivities(24); // Last 24 hours
        
        System.out.println("\n========== Recent Activities (Last 24 Hours - Real-time) ==========");
        System.out.println("Timestamp           | Type              | User       | Details");
        System.out.println("-------------------|-------------------|------------|----------------------");
        
        int displayCount = Math.min(20, activities.size()); // Display last 20
        for (int i = 0; i < displayCount; i++) {
            AdminService.ActivityLog log = activities.get(i);
            System.out.printf("%s | %-17s | %-10s | %s%n",
                log.timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                log.type,
                log.user,
                log.details
            );
        }
        
        System.out.println("\nShowing " + displayCount + " of " + activities.size() + " recent activities");
    }
    
    private void viewPopularSearches() throws SQLException {
        List<SearchHistoryRepository.PopularSearchTerm> terms = adminService.getPopularSearchTerms(10);
        
        System.out.println("\n========== Popular Search Terms (Real-time) ==========");
        System.out.println("Rank | Search Term   | Count | Last Searched");
        System.out.println("-----|---------------|-------|---------------");
        
        int rank = 1;
        for (SearchHistoryRepository.PopularSearchTerm term : terms) {
            System.out.printf("%d | %-13s | %d | %s%n",
                rank,
                term.getTerm(),
                term.getCount(),
                term.getLastSearched().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
            rank++;
        }
        
        if (terms.isEmpty()) {
            System.out.println("No search data available yet");
        }
    }
    
    private void viewSystemStatistics() throws SQLException {
        AdminService.SystemStatistics stats = adminService.getSystemStatistics();
        
        System.out.println("\n========== System Statistics (Real-time) ==========");
        System.out.println("Total Users:           " + stats.getTotalUsers());
        System.out.println("Total Advertisers:     " + stats.getTotalAdvertisers());
        System.out.println("Total Ads:             " + stats.getTotalAds());
        System.out.println("Total Ad Conflicts:    " + stats.getTotalConflicts());
        System.out.println("Total Impressions:     " + stats.getTotalImpressions());
        System.out.println("Total Clicks:          " + stats.getTotalClicks());
        System.out.printf("Overall CTR:           %.2f%%%n", stats.getOverallCTR());
        System.out.printf("Total Revenue:         $%.2f%n", stats.getTotalRevenue());
    }
    
    private void viewMarkovAnalytics() throws SQLException {
        System.out.println("\n========== Markov Analytics Dashboard ==========");
        System.out.println("1. State Transition Analysis");
        System.out.println("2. User Intent Distribution");
        System.out.println("3. Conversion Funnel Analysis");
        System.out.println("4. Ad Type Effectiveness");
        System.out.println("5. State Transition Recommendations");
        System.out.println("6. Back to Main Menu");
        System.out.print("\nChoose option: ");
        
        String choice = scanner.nextLine().trim();
        
        try {
            switch (choice) {
                case "1":
                    showStateTransitionAnalysis();
                    break;
                case "2":
                    showUserIntentDistribution();
                    break;
                case "3":
                    showConversionFunnelAnalysis();
                    break;
                case "4":
                    showAdTypeEffectiveness();
                    break;
                case "5":
                    showRecommendations();
                    break;
                case "6":
                    break;
                default:
                    System.out.println("Invalid option");
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }
    
    private void showStateTransitionAnalysis() throws SQLException {
        MarkovAnalyticsService.StateTransitionAnalysis analysis = markovAnalyticsService.getStateTransitionAnalysis();
        model.MarkovTransitionMatrix matrix = analysis.getTransitionMatrix();
        
        System.out.println("\n========== State Transition Analysis ==========");
        System.out.println("User Intent State Transitions (Probabilities):");
        System.out.println();
        
        for (UserIntentState from : UserIntentState.values()) {
            System.out.print(from + " -> ");
            for (UserIntentState to : UserIntentState.values()) {
                double prob = matrix.getTransitionProbability(from, to);
                System.out.printf("%s: %.1f%% | ", to, prob * 100);
            }
            System.out.println();
        }
        
        System.out.println("\nData collected at: " + analysis.getDataCollectedAt());
    }
    
    private void showUserIntentDistribution() throws SQLException {
        MarkovAnalyticsService.UserIntentDistribution distribution = markovAnalyticsService.getUserIntentDistribution();
        
        System.out.println("\n========== User Intent State Distribution ==========");
        
        Map<UserIntentState, Double> states = distribution.getStateDistribution();
        for (Map.Entry<UserIntentState, Double> entry : states.entrySet()) {
            System.out.printf("%s: %.1f%%%n", entry.getKey(), entry.getValue());
        }
    }
    
    private void showConversionFunnelAnalysis() throws SQLException {
        MarkovAnalyticsService.ConversionFunnelAnalysis analysis = markovAnalyticsService.getConversionFunnelAnalysis();
        
        System.out.println("\n========== Conversion Funnel Analysis ==========");
        System.out.println("Funnel Stages: " + analysis.getFunnelStages());
        System.out.println("\nDrop-off Rates:");
        
        Map<String, Double> dropOffRates = analysis.getDropOffRates();
        for (Map.Entry<String, Double> entry : dropOffRates.entrySet()) {
            System.out.printf("%s: %.1f%%%n", entry.getKey(), entry.getValue() * 100);
        }
    }
    
    private void showAdTypeEffectiveness() throws SQLException {
        MarkovAnalyticsService.AdTypeEffectivenessAnalysis analysis = markovAnalyticsService.getAdTypeEffectiveness();
        
        System.out.println("\n========== Ad Type Effectiveness by User State ==========");
        
        Map<String, Map<UserIntentState, Double>> effectiveness = analysis.getAdTypeEffectiveness();
        for (Map.Entry<String, Map<UserIntentState, Double>> adTypeEntry : effectiveness.entrySet()) {
            System.out.println("\n" + adTypeEntry.getKey().toUpperCase() + " Ads:");
            for (Map.Entry<UserIntentState, Double> stateEntry : adTypeEntry.getValue().entrySet()) {
                System.out.printf("  %s: %.1f%%%n", stateEntry.getKey(), stateEntry.getValue() * 100);
            }
        }
    }
    
    private void showRecommendations() throws SQLException {
        MarkovAnalyticsService.StateTransitionRecommendation recommendation = markovAnalyticsService.getRecommendations();
        
        System.out.println("\n========== State Transition Recommendations ==========");
        
        List<String> recommendations = recommendation.getRecommendations();
        if (recommendations.isEmpty()) {
            System.out.println("No recommendations at this time.");
        } else {
            for (String rec : recommendations) {
                System.out.println("• " + rec);
            }
        }
    }
}
