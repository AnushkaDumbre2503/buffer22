package dsa.trie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrieNode {
    private Map<Character, TrieNode> children;
    private boolean isEndOfWord;
    private List<String> keywords;
    
    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
        this.keywords = new ArrayList<>();
    }
    
    public Map<Character, TrieNode> getChildren() {
        return children;
    }
    
    public void setChildren(Map<Character, TrieNode> children) {
        this.children = children;
    }
    
    public boolean isEndOfWord() {
        return isEndOfWord;
    }
    
    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }
    
    public List<String> getKeywords() {
        return keywords;
    }
    
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    
    public void addKeyword(String keyword) {
        if (!keywords.contains(keyword)) {
            keywords.add(keyword);
        }
    }
    
    public boolean hasChild(char ch) {
        return children.containsKey(ch);
    }
    
    public TrieNode getChild(char ch) {
        return children.get(ch);
    }
    
    public void addChild(char ch, TrieNode node) {
        children.put(ch, node);
    }
    
    public int getChildCount() {
        return children.size();
    }
    
    public boolean isLeaf() {
        return children.isEmpty();
    }
    
    @Override
    public String toString() {
        return "TrieNode{end=" + isEndOfWord + ", keywords=" + keywords + ", children=" + children.keySet() + "}";
    }
}
