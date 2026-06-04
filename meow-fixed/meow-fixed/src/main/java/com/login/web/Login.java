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

@Document(collection = "login")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"carts", "bookings"})
public class Login {

    public static final String SEQUENCE_NAME = "users_sequence";

    @Id
    private int id;

    @Indexed
    private String fname;
    private String lname;
    private String Address;
    private String phoneno;

    @Indexed(unique = true)
    private String email;
    private String password;

    private byte[] image;

    @ReadOnlyProperty
    @DocumentReference(lookup = "{ 'login' : ?#{#self._id} }")
    private List<Cart> carts;

    @ReadOnlyProperty
    @DocumentReference(lookup = "{ 'login' : ?#{#self._id} }")
    private List<Booking> bookings;
}