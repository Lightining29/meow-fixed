package com.login.web;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends MongoRepository<Cart, Integer> {
    List<Cart> findByLogin(Login login);
    void deleteByProductId(int id);
    void deleteByProduct_Id(int id);
}
