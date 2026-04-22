package model;

public enum AdSlot {
    TOP("TOP", 1.5, 3),      // High visibility, high fatigue
    SIDEBAR("SIDEBAR", 1.0, 2), // Balanced
    FOOTER("FOOTER", 0.7, 1);   // Low visibility, low fatigue
    
    private final String name;
    private final double weight;      // Scoring weight
    private final int cooldownSeconds; // Cooldown period in seconds
    
    AdSlot(String name, double weight, int cooldownSeconds) {
        this.name = name;
        this.weight = weight;
        this.cooldownSeconds = cooldownSeconds;
    }
    
    public String getName() { return name; }
    public double getWeight() { return weight; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    
    @Override
    public String toString() {
        return name + " (weight=" + weight + ", cooldown=" + cooldownSeconds + "s)";
    }
}
