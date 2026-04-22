package app;

import cli.RoleBasedCLI;
import database.DBConnection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Intelligent Ad Allocation Engine...");
        
        try {
            // Initialize database connection
            DBConnection.initializeDatabase();
            
            // Start role-based CLI interface
            RoleBasedCLI cli = new RoleBasedCLI();
            cli.start();
            
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // Close database connection
            DBConnection.closeConnection();
        }
    }
}
