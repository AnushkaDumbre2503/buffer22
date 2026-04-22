package dsa.trie;

import java.util.*;

public class KeywordTrie {
    private TrieNode root;
    
    public KeywordTrie() {
        this.root = new TrieNode();
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
    
    public boolean contains(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        
        keyword = keyword.toLowerCase().trim();
        TrieNode current = root;
        
        for (char ch : keyword.toCharArray()) {
            if (!current.getChildren().containsKey(ch)) {
                return false;
            }
            current = current.getChildren().get(ch);
        }
        
        return current.isEndOfWord();
    }
    
    public List<String> search(String text) {
        List<String> foundKeywords = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return foundKeywords;
        }
        
        text = text.toLowerCase();
        TrieNode current = root;
        
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            
            // If character not found, reset to root
            if (!current.getChildren().containsKey(ch)) {
                current = root;
                continue;
            }
            
            current = current.getChildren().get(ch);
            
            // If we found a word end, add all keywords at this node
            if (current.isEndOfWord()) {
                foundKeywords.addAll(current.getKeywords());
            }
        }
        
        return foundKeywords;
    }
    
    public Map<String, Integer> getKeywordFrequency(String text) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        if (text == null || text.trim().isEmpty()) {
            return frequencyMap;
        }
        
        text = text.toLowerCase();
        
        for (int i = 0; i < text.length(); i++) {
            TrieNode current = root;
            
            for (int j = i; j < text.length(); j++) {
                char ch = text.charAt(j);
                
                if (!current.getChildren().containsKey(ch)) {
                    break;
                }
                
                current = current.getChildren().get(ch);
                
                if (current.isEndOfWord()) {
                    for (String keyword : current.getKeywords()) {
                        frequencyMap.put(keyword, frequencyMap.getOrDefault(keyword, 0) + 1);
                    }
                }
            }
        }
        
        return frequencyMap;
    }
    
    public void insertAll(Collection<String> keywords) {
        for (String keyword : keywords) {
            insert(keyword);
        }
    }
    
    public void clear() {
        this.root = new TrieNode();
    }
    
    public int size() {
        return countNodes(root);
    }
    
    private int countNodes(TrieNode node) {
        int count = 1;
        for (TrieNode child : node.getChildren().values()) {
            count += countNodes(child);
        }
        return count;
    }
    
    @Override
    public String toString() {
        return "KeywordTrie{size=" + size() + "}";
    }
}
