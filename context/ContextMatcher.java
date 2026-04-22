package context;

import java.util.*;
import model.Ad;

public class ContextMatcher {
    private AhoCorasickTrie keywordTrie;
    
    public ContextMatcher() {
        this.keywordTrie = new AhoCorasickTrie();
    }
    
    public void buildKeywordIndex(List<Ad> ads) {
        Set<String> allKeywords = new HashSet<>();
        
        for (Ad ad : ads) {
            if (ad.getKeywords() != null) {
                for (String keyword : ad.getKeywords()) {
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        allKeywords.add(keyword.toLowerCase().trim());
                    }
                }
            }
        }
        
        keywordTrie.insertAll(allKeywords);
    }
    
    public Map<Integer, Double> calculateContextScores(String searchText, String pageContent, List<Ad> ads) {
        Map<Integer, Double> contextScores = new HashMap<>();
        
        // Combine search text and page content
        String combinedText = "";
        if (searchText != null) combinedText += searchText + " ";
        if (pageContent != null) combinedText += pageContent;
        
        // Extract keyword frequencies using Aho-Corasick
        Map<String, Integer> keywordCounts = keywordTrie.searchKeywords(combinedText);
        
        // Calculate context score for each ad
        for (Ad ad : ads) {
            double contextScore = calculateAdContextScore(keywordCounts, ad, combinedText);
            contextScores.put(ad.getId(), contextScore);
        }
        
        return contextScores;
    }
    
    private double calculateAdContextScore(Map<String, Integer> keywordCounts, Ad ad, String combinedText) {
        if (ad.getKeywords() == null || ad.getKeywords().isEmpty()) {
            return 0.5; // Neutral score for ads with no keywords - can still win on bid
        }
        
        double totalScore = 0.0;
        int matchedKeywords = 0;
        
        for (String keyword : ad.getKeywords()) {
            if (keyword == null || keyword.trim().isEmpty()) continue;
            
            keyword = keyword.toLowerCase().trim();
            int count = keywordCounts.getOrDefault(keyword, 0);
            
            if (count > 0) {
                // Direct match found - give full points
                totalScore += Math.min(count, 5); // Cap at 5 matches per keyword
                matchedKeywords++;
            } else {
                // Check for partial/fuzzy match
                double partialScore = getPartialMatchScore(keyword, combinedText);
                if (partialScore > 0) {
                    totalScore += partialScore;
                    matchedKeywords++;
                }
            }
        }
        
        // BETTER NORMALIZATION FORMULA
        // Base score increases with number of matched keywords
        // Unmatched keywords reduce the score proportionally
        double baseScore = (double) matchedKeywords / ad.getKeywords().size();
        double weightedScore = totalScore / ad.getKeywords().size();
        
        // Combine: 60% based on number of matches, 40% on match weight
        double normalizedScore = (baseScore * 0.6) + (Math.min(weightedScore, 3.0) * 0.4 / 3.0);
        
        // Scale to 0.3 - 3.0 range (multiplier for scoring)
        // Ensures even partial matches get decent scores, but perfect matches score highest
        double finalScore = 0.3 + (normalizedScore * 2.7);
        
        return Math.max(0.3, Math.min(3.0, finalScore));
    }
    
    private double getPartialMatchScore(String keyword, String text) {
        if (text == null || text.isEmpty()) return 0.0;
        
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        
        // Exact substring match
        if (lowerText.contains(lowerKeyword)) {
            return 2.0; // Strong match
        }
        
        // Check for word-based similarity (ignoring common prefixes/suffixes)
        String[] textWords = lowerText.split("[\\s,;.!?\\-'\"()]+");
        
        for (String word : textWords) {
            // Exact word match
            if (word.equals(lowerKeyword)) {
                return 2.0;
            }
            
            // Prefix match (e.g., "camera" matches "cameraman")
            if (word.startsWith(lowerKeyword) && word.length() <= lowerKeyword.length() + 3) {
                return 1.5;
            }
            
            // Suffix match (e.g., "phone" in "smartphone")
            if (word.endsWith(lowerKeyword) && word.length() <= lowerKeyword.length() + 3) {
                return 1.5;
            }
            
            // Partial word match (e.g., "cam" in "camera")
            if (lowerKeyword.length() >= 3 && word.contains(lowerKeyword)) {
                return 1.0;
            }
        }
        
        // No match found
        return 0.0;
    }
    
    public double getContextMatchScore(Ad ad, String searchText, String pageContent) {
        String combinedText = "";
        if (searchText != null) combinedText += searchText + " ";
        if (pageContent != null) combinedText += pageContent;
        
        Map<String, Integer> keywordCounts = keywordTrie.searchKeywords(combinedText);
        return calculateAdContextScore(keywordCounts, ad, combinedText);
    }
    
    public Map<String, Integer> getKeywordFrequency(String text) {
        return keywordTrie.searchKeywords(text);
    }
    
    public void rebuildIndex(List<Ad> ads) {
        keywordTrie.clear();
        buildKeywordIndex(ads);
    }
    
    public void addKeywords(List<String> keywords) {
        keywordTrie.insertAll(keywords);
    }
    
    public void clear() {
        keywordTrie.clear();
    }
}
