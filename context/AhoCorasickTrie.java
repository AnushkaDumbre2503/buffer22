package context;

import dsa.trie.TrieNode;
import java.util.*;

public class AhoCorasickTrie {
    private TrieNode root;
    private Map<TrieNode, TrieNode> failureLinks;
    
    public AhoCorasickTrie() {
        this.root = new TrieNode();
        this.failureLinks = new HashMap<>();
        buildFailureLinks();
    }
    
    public void insert(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        
        keyword = keyword.toLowerCase().trim();
        TrieNode current = root;
        
        for (char ch : keyword.toCharArray()) {
            current = current.getChildren().computeIfAbsent(ch, c -> new TrieNode());
        }
        
        current.setEndOfWord(true);
        current.addKeyword(keyword);
    }
    
    public void insertAll(Collection<String> keywords) {
        for (String keyword : keywords) {
            insert(keyword);
        }
        buildFailureLinks();
    }
    
    private void buildFailureLinks() {
        Queue<TrieNode> queue = new LinkedList<>();
        
        // Initialize failure links for root's children
        for (TrieNode child : root.getChildren().values()) {
            failureLinks.put(child, root);
            queue.add(child);
        }
        
        // BFS to build failure links
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();
            
            for (Map.Entry<Character, TrieNode> entry : current.getChildren().entrySet()) {
                char ch = entry.getKey();
                TrieNode child = entry.getValue();
                
                // Find failure link for child
                TrieNode failure = failureLinks.get(current);
                while (failure != root && !failure.hasChild(ch)) {
                    failure = failureLinks.get(failure);
                }
                
                if (failure.hasChild(ch)) {
                    failureLinks.put(child, failure.getChild(ch));
                } else {
                    failureLinks.put(child, root);
                }
                
                queue.add(child);
            }
        }
    }
    
    public Map<String, Integer> searchKeywords(String text) {
        Map<String, Integer> keywordCounts = new HashMap<>();
        if (text == null || text.trim().isEmpty()) {
            return keywordCounts;
        }
        
        text = text.toLowerCase();
        TrieNode current = root;
        
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            
            // Follow failure links if character not found
            while (current != root && !current.hasChild(ch)) {
                current = failureLinks.get(current);
            }
            
            if (current.hasChild(ch)) {
                current = current.getChild(ch);
            }
            
            // Check for matches at current position
            TrieNode matchNode = current;
            while (matchNode != root) {
                if (matchNode.isEndOfWord()) {
                    for (String keyword : matchNode.getKeywords()) {
                        keywordCounts.put(keyword, keywordCounts.getOrDefault(keyword, 0) + 1);
                    }
                }
                matchNode = failureLinks.get(matchNode);
            }
        }
        
        return keywordCounts;
    }
    
    public double calculateContextScore(Map<String, Integer> keywordCounts, List<String> targetKeywords) {
        if (targetKeywords == null || targetKeywords.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        int matchedKeywords = 0;
        
        for (String keyword : targetKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) continue;
            
            keyword = keyword.toLowerCase().trim();
            int count = keywordCounts.getOrDefault(keyword, 0);
            if (count > 0) {
                totalScore += count;
                matchedKeywords++;
            }
        }
        
        // Normalize by number of target keywords
        return targetKeywords.size() > 0 ? totalScore / targetKeywords.size() : 0.0;
    }
    
    public void clear() {
        this.root = new TrieNode();
        this.failureLinks.clear();
    }
    
    @Override
    public String toString() {
        return "AhoCorasickTrie{root=" + root + "}";
    }
}
