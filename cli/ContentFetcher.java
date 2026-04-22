package cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ContentFetcher {
    
    public static String fetchContent(String searchQuery) {
        try {
            // Simple implementation using public search APIs
            // In production, integrate with Jsoup for better parsing
            String keywords = extractKeywords(searchQuery);
            return keywords;
        } catch (Exception e) {
            System.out.println("Warning: Could not fetch external content. Using search query as keywords.");
            return extractKeywords(searchQuery);
        }
    }
    
    private static String extractKeywords(String text) {
        // Extract relevant keywords from text
        List<String> keywords = new ArrayList<>();
        
        // Split on common delimiters and filter
        String[] words = text.toLowerCase().split("[\\s,;.!?-]+");
        
        for (String word : words) {
            if (word.length() > 3 && !isStopword(word)) {
                keywords.add(word);
            }
        }
        
        return String.join(", ", keywords);
    }
    
    private static boolean isStopword(String word) {
        String[] stopwords = {"the", "and", "or", "is", "a", "an", "in", "on", "at", "for", "to", "of", "by", "with", "from"};
        for (String stopword : stopwords) {
            if (word.equals(stopword)) {
                return true;
            }
        }
        return false;
    }
    
    public static String fetchWebContent(String url) {
        try {
            URL urlObj = new URL(url);
            URLConnection connection = urlObj.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
                if (content.length() > 5000) break; // Limit content
            }
            reader.close();
            
            return cleanHTML(content.toString());
        } catch (Exception e) {
            return "";
        }
    }
    
    private static String cleanHTML(String html) {
        // Remove HTML tags
        return html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ");
    }
}
