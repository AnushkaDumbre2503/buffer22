package context;

import model.Ad;
import java.util.*;

public class ContextMatcher {
    private AhoCorasickTrie keywordTrie;
    
    public ContextMatcher() {
        this.keywordTrie = new AhoCorasickTrie();
    }
    
    public void buildKeywordIndex(List<Ad> ads) {
        Set<String> allKeywords = new HashSet<>();
        
        for (Ad ad : ads) {
            if (ad.getKeywords() != null) {
                allKeywords.addAll(ad.getKeywords());
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
            double contextScore = keywordTrie.calculateContextScore(keywordCounts, ad.getKeywords());
            contextScores.put(ad.getId(), contextScore);
        }
        
        return contextScores;
    }
    
    public double getContextMatchScore(Ad ad, String searchText, String pageContent) {
        String combinedText = "";
        if (searchText != null) combinedText += searchText + " ";
        if (pageContent != null) combinedText += pageContent;
        
        Map<String, Integer> keywordCounts = keywordTrie.searchKeywords(combinedText);
        return keywordTrie.calculateContextScore(keywordCounts, ad.getKeywords());
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
