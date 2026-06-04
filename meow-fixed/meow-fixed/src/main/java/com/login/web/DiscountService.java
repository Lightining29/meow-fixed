package com.login.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    /**
     * Apply discount code to order
     */
    public DiscountResult applyDiscount(String code, int orderAmount) {
        Optional<Discount> discountOpt = discountRepository.findByCodeAndActiveTrue(code);
        
        if (discountOpt.isEmpty()) {
            return new DiscountResult(false, 0, "Invalid discount code");
        }

        Discount discount = discountOpt.get();
        
        // Check validity period
        LocalDateTime now = LocalDateTime.now();
        if (discount.getValidFrom() != null && now.isBefore(discount.getValidFrom())) {
            return new DiscountResult(false, 0, "Discount not yet valid");
        }
        if (discount.getValidUntil() != null && now.isAfter(discount.getValidUntil())) {
            return new DiscountResult(false, 0, "Discount has expired");
        }

        // Check minimum order amount
        if (orderAmount < discount.getMinOrderAmount()) {
            return new DiscountResult(false, 0, 
                "Minimum order amount ₹" + discount.getMinOrderAmount() + " required");
        }

        // Calculate discount
        int discountAmount = (orderAmount * discount.getPercentage()) / 100;
        
        // Apply max discount limit
        if (discount.getMaxDiscountAmount() > 0 && discountAmount > discount.getMaxDiscountAmount()) {
            discountAmount = discount.getMaxDiscountAmount();
        }

        return new DiscountResult(true, discountAmount, "Discount applied successfully!");
    }

    /**
     * Get all active discounts
     */
    public List<Discount> getActiveDiscounts() {
        return discountRepository.findByActiveTrue();
    }

    /**
     * Get discounts for a specific product
     */
    public List<Discount> getProductDiscounts(Product product) {
        return discountRepository.findByProductAndActiveTrue(product);
    }

    /**
     * Create a new discount
     */
    public Discount createDiscount(Discount discount) {
        return discountRepository.save(discount);
    }

    // Inner class for discount result
    public static class DiscountResult {
        private boolean success;
        private int discountAmount;
        private String message;

        public DiscountResult(boolean success, int discountAmount, String message) {
            this.success = success;
            this.discountAmount = discountAmount;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getDiscountAmount() {
            return discountAmount;
        }

        public String getMessage() {
            return message;
        }
    }
}
