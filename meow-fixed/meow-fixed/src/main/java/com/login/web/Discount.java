package com.login.web;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Document(collection = "discount")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Discount {

    public static final String SEQUENCE_NAME = "discounts_sequence";

    @Id
    private int id;
    
    private String code;
    private int percentage;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean active = false;
    private int minOrderAmount;
    private int maxDiscountAmount;
    
    @DocumentReference
    private Product product; // null if discount applies to all products
}