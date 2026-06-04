package com.login.web;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyRepository extends MongoRepository<Login, Integer> {
    Optional<Login> findByFnameAndPassword(String fname, String password);
    void deleteByEmail(String email);
    Optional<Login> findByemail(String email);
    Login findByEmail(String email);
}
