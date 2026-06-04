package com.login.web;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends MongoRepository<Discount, Integer> {
    Optional<Discount> findByCodeAndActiveTrue(String code);
    List<Discount> findByActiveTrue();
    List<Discount> findByProductAndActiveTrue(Product product);
}
