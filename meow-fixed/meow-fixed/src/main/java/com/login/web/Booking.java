package com.login.web;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import lombok.*;

@Document(collection = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Booking {

    public static final String SEQUENCE_NAME = "bookings_sequence";

    @Id
    private int id;

    private String address;
    private String paymentMethod;
    private double totalAmount;
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Indexed
    @DocumentReference
    private Login login;

    @DocumentReference
    private List<Product> product;
}
