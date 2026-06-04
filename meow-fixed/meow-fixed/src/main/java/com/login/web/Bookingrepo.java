package com.login.web;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Bookingrepo extends MongoRepository<Booking, Integer> {
    List<Booking> findByLogin(Login loggedInUser);
}
