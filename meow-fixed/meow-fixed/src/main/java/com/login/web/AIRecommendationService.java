package com.login.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {

    private final ChatClient chatClient;
    
    @Autowired
    private Productrepository productRepository;

    public AIRecommendationService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * Get AI-powered food recommendations based on user preferences
     */
    public List<String> getRecommendations(String userPreference, List<Product> availableProducts) {
        try {
            String productList = availableProducts.stream()
                .map(p -> p.getFood() + " (₹" + p.getPrice() + ")")
                .collect(Collectors.joining(", "));

            String prompt = String.format(
                "Based on the user preference: '%s', recommend 3-5 food items from this menu: %s. " +
                "Provide only the food names as a comma-separated list, nothing else.",
                userPreference, productList
            );

            String response = chatClient.prompt(prompt).call().content();
            
            // Parse the response into a list
            String[] recommendations = response.split(",");
            List<String> result = new ArrayList<>();
            for (String rec : recommendations) {
                result.add(rec.trim());
            }
            return result;
        } catch (Exception e) {
            return List.of("Unable to generate recommendations at this time");
        }
    }

    /**
     * Generate AI-powered food description
     */
    public String generateFoodDescription(String foodName) {
        try {
            String prompt = String.format(
                "Write a short, appetizing 2-sentence description for '%s'. Make it sound delicious and appealing.",
                foodName
            );
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            return "Delicious food item that you'll love!";
        }
    }

    /**
     * Get smart search suggestions
     */
    public List<String> getSearchSuggestions(String query, List<Product> allProducts) {
        try {
            String productList = allProducts.stream()
                .map(Product::getFood)
                .distinct()
                .collect(Collectors.joining(", "));

            String prompt = String.format(
                "User typed: '%s'. From this menu: %s, suggest 3-5 relevant food items they might be looking for. " +
                "Return only food names as comma-separated list.",
                query, productList
            );

            String response = chatClient.prompt(prompt).call().content();
            String[] suggestions = response.split(",");
            List<String> result = new ArrayList<>();
            for (String sug : suggestions) {
                result.add(sug.trim());
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Suggest meal combos/pairings
     */
    public String suggestMealCombo(String mainDish) {
        try {
            String prompt = String.format(
                "For someone ordering '%s', suggest 2-3 complementary items (drinks, sides, or desserts) " +
                "that would pair well. Keep it brief and appetizing.",
                mainDish
            );
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            return "Try adding a refreshing beverage or dessert to complete your meal!";
        }
    }

    /**
     * Generate personalized recommendations based on order history
     */
    public List<String> getPersonalizedRecommendations(List<String> orderHistory) {
        try {
            String history = String.join(", ", orderHistory);
            String prompt = String.format(
                "User has previously ordered: %s. Based on their taste, suggest 3-5 new food items they might enjoy. " +
                "Return only food names as comma-separated list.",
                history
            );

            String response = chatClient.prompt(prompt).call().content();
            String[] recommendations = response.split(",");
            List<String> result = new ArrayList<>();
            for (String rec : recommendations) {
                result.add(rec.trim());
            }
            return result;
        } catch (Exception e) {
            return List.of("Explore our menu for more delicious options!");
        }
    }

    /**
     * Get AI-powered discount suggestions
     */
    public String suggestDiscount(int totalAmount, List<Product> cartItems) {
        try {
            String items = cartItems.stream()
                .map(Product::getFood)
                .collect(Collectors.joining(", "));

            String prompt = String.format(
                "Customer is ordering: %s for ₹%d. Suggest a creative discount offer or combo deal " +
                "that would encourage them to complete the purchase. Keep it brief and enticing.",
                items, totalAmount
            );
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            return "Complete your order now and enjoy great savings!";
        }
    }
}
