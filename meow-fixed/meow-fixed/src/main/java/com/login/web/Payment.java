package com.login.web;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "payment")
public class Payment {

    public static final String SEQUENCE_NAME = "payments_sequence";

    @Id
    private Long id;

    @Indexed
    private Long orderId;
    private String utr;
    private double amount;

    @Indexed
    private String status;

    private LocalDateTime createdAt = LocalDateTime.now();
}
