package cli;

import auth.AuthContext;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import model.Ad;
import model.AdSlot;
import model.AllocationResult;
import service.AdAllocationService;

public class UserCLI {
    private AuthContext authContext;
    private Scanner scanner;
    private AdAllocationService adService;
    
    // ===== STATE MANAGEMENT =====
    // Persists across menu iterations (NOT cleared after each operation)
    private Map<AdSlot, Ad> allocatedAdsBySlot;  // Ads allocated to each slot
    private boolean hasAllocatedAds;  // Flag to check if ads are available
    
    public UserCLI(AuthContext authContext, Scanner scanner) {
        this.authContext = authContext;
        this.scanner = scanner;
        this.adService = new AdAllocationService();
        this.allocatedAdsBySlot = new HashMap<>();
        this.hasAllocatedAds = false;
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
                    handleSearch();
                    break;
                case "2":
                    handleClick();
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
            e.printStackTrace();
        }
    }
    
    // ========== SEARCH FLOW ==========
    
    /**
     * Main search handler
     */
    private void handleSearch() throws Exception {
        System.out.print("Enter search query: ");
        String query = scanner.nextLine().trim();
        
        if (query.isEmpty()) {
            System.out.println("Search query cannot be empty");
            return;
        }
        
        try {
            System.out.println("\nFetching content for: " + query);
            String content = ContentFetcher.fetchContent(query);
            
            // Allocate ads for all slots
            allocateAdsToAllSlots(query, content);
            
            // Display all allocated ads
            displayAllocatedAds();
            
        } catch (SQLException e) {
            System.out.println("Error allocating ads: " + e.getMessage());
        }
    }
    
    /**
     * Allocates ads to all three slots (TOP, SIDEBAR, FOOTER)
     * Stores results in allocatedAdsBySlot
     */
    private void allocateAdsToAllSlots(String query, String content) throws SQLException {
        // Clear previous allocation
        allocatedAdsBySlot.clear();
        
        try {
            // Allocate ads using the service
            AllocationResult result = adService.allocateAds(authContext.getUsername(), query, content);
            
            // Debug: Check if result is null
            if (result == null) {
                System.out.println("DEBUG: AllocationResult is null!");
                hasAllocatedAds = false;
                return;
            }
            
            // Debug: Check allocated ads
            Map<AdSlot, Ad> resultAds = result.getAllocatedAds();
            System.out.println("DEBUG: Result has " + (resultAds != null ? resultAds.size() : "null") + " allocated ads");
            
            if (resultAds == null || resultAds.isEmpty()) {
                System.out.println("DEBUG: No ads in allocation result");
                hasAllocatedAds = false;
                return;
            }
            
            // Store allocated ads by slot
            for (AdSlot slot : AdSlot.values()) {
                if (resultAds.containsKey(slot)) {
                    Ad ad = resultAds.get(slot);
                    allocatedAdsBySlot.put(slot, ad);
                    System.out.println("DEBUG: Stored ad for slot " + slot.getName() + ": " + ad.getTitle());
                }
            }
            
            hasAllocatedAds = !allocatedAdsBySlot.isEmpty();
            System.out.println("DEBUG: hasAllocatedAds = " + hasAllocatedAds + ", map size = " + allocatedAdsBySlot.size());
            
        } catch (Exception e) {
            System.out.println("DEBUG: Exception during allocation: " + e.getMessage());
            e.printStackTrace();
            hasAllocatedAds = false;
        }
    }
    
    /**
     * Displays all allocated ads across all slots
     */
    private void displayAllocatedAds() {
        System.out.println("\n========== ADS ALLOCATED ==========");
        
        if (allocatedAdsBySlot.isEmpty()) {
            System.out.println("No ads available for your search. Try a different query!");
            hasAllocatedAds = false;
            System.out.println("====================================\n");
            return;
        }
        
        int slotNumber = 1;
        for (AdSlot slot : AdSlot.values()) {
            if (allocatedAdsBySlot.containsKey(slot)) {
                Ad ad = allocatedAdsBySlot.get(slot);
                System.out.println("\n" + slotNumber + ". Slot: " + slot.getName() + 
                                 " (weight=" + slot.getWeight() + ", cooldown=" + slot.getCooldownSeconds() + "s)");
                System.out.println("   Ad Title: " + ad.getTitle());
                System.out.println("   Ad ID: " + ad.getId());
                System.out.println("   Keywords: " + String.join(", ", ad.getKeywords()));
                System.out.println("   Bid Amount: $" + String.format("%.2f", ad.getBidAmount()));
                slotNumber++;
            }
        }
        
        System.out.println("\n====================================\n");
        hasAllocatedAds = true;
    }
    
    // ========== CLICK FLOW ==========
    
    /**
     * Main click handler
     */
    private void handleClick() {
        // Check if ads were allocated
        if (!hasAllocatedAds || allocatedAdsBySlot.isEmpty()) {
            System.out.println("\nNo ads to click on. Please search first!\n");
            return;
        }
        
        try {
            // Step 1: Ask which slot to click
            AdSlot selectedSlot = selectSlot();
            if (selectedSlot == null) {
                return;  // User cancelled
            }
            
            Ad selectedAd = allocatedAdsBySlot.get(selectedSlot);
            
            // Step 2: Display ad details
            displayAdDetails(selectedSlot, selectedAd);
            
            // Step 3: Register click
            if (confirmClick()) {
                recordAdClick(selectedAd.getId());
            } else {
                System.out.println("Click cancelled\n");
            }
            
        } catch (SQLException e) {
            System.out.println("Error recording click: " + e.getMessage());
        }
    }
    
    /**
     * Step 1: Ask user which slot to click
     */
    private AdSlot selectSlot() {
        System.out.println("\nAvailable Slots to Click:");
        int slotIndex = 1;
        Map<Integer, AdSlot> slotMap = new HashMap<>();
        
        for (AdSlot slot : AdSlot.values()) {
            if (allocatedAdsBySlot.containsKey(slot)) {
                System.out.println(slotIndex + ". " + slot.getName());
                slotMap.put(slotIndex, slot);
                slotIndex++;
            }
        }
        
        System.out.print("Select slot (1-" + (slotIndex - 1) + "): ");
        
        try {
            String slotChoice = scanner.nextLine().trim();
            int selectedSlotNum = Integer.parseInt(slotChoice);
            
            if (!slotMap.containsKey(selectedSlotNum)) {
                System.out.println("Invalid slot selection");
                return null;
            }
            
            return slotMap.get(selectedSlotNum);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            return null;
        }
    }
    
    /**
     * Step 2: Display ad details
     */
    private void displayAdDetails(AdSlot slot, Ad ad) {
        System.out.println("\n========== AD DETAILS ==========");
        System.out.println("Slot: " + slot.getName());
        System.out.println("Ad Title: " + ad.getTitle());
        System.out.println("Ad ID: " + ad.getId());
        System.out.println("Keywords: " + String.join(", ", ad.getKeywords()));
        System.out.println("Bid Amount: $" + String.format("%.2f", ad.getBidAmount()));
        System.out.println("================================\n");
    }
    
    /**
     * Step 3: Ask confirmation
     */
    private boolean confirmClick() {
        System.out.print("Click this ad? (y/n): ");
        String confirmChoice = scanner.nextLine().trim().toLowerCase();
        return confirmChoice.equals("y") || confirmChoice.equals("yes");
    }
    
    /**
     * Record the click in database
     */
    private void recordAdClick(int adId) throws SQLException {
        adService.recordClick(authContext.getUsername(), adId);
        System.out.println("\n✓ Click registered for Ad ID: " + adId);
        System.out.println("✓ Thank you for your interest!\n");
    }
}
