package com.login.web;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import lombok.*;

@Document(collection = "cart")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"login", "product"})
public class Cart {

    public static final String SEQUENCE_NAME = "carts_sequence";

    @Id
    private int id;

    @Indexed
    @DocumentReference
    private Login login;

    @DocumentReference
    private Product product;

    private int quantity;
    private double price;        // original price
    private double finalPrice;   // after discount (per item)
    private double totalPrice;   // final total
}