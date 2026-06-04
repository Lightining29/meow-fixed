package com.login.web;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document(collection = "cat")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"products"})
public class Cat {

    public static final String SEQUENCE_NAME = "categories_sequence";

    @Id
    private int id;
    private String category;

    @ReadOnlyProperty
    @DocumentReference(lookup = "{ 'cat' : ?#{#self._id} }")
    private List<Product> products;
}
