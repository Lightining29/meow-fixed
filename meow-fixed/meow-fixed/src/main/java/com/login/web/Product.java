package com.login.web;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document(collection = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"bookings", "discount", "cat"})
public class Product {

    public static final String SEQUENCE_NAME = "products_sequence";

    @Id
    private int id;

    @Indexed
    private String food;

    @Indexed
    private int price;
    private String description;
    private byte[] image;

    @Indexed
    @DocumentReference
    private Cat cat;

    @ReadOnlyProperty
    @DocumentReference(lookup = "{ 'product' : ?#{#self._id} }")
    private List<Booking> bookings;

    @DocumentReference
    private Discount discount;
}
