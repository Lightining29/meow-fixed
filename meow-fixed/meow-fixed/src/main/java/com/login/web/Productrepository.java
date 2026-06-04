package com.login.web;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Productrepository extends MongoRepository<Product, Integer> {
    List<Product> findByCat(Cat category);
    List<Product> findByPriceGreaterThanEqual(int price);
    List<Product> findByPriceLessThanEqual(int price);
    List<Product> findByFoodContainingIgnoreCase(String keyword);
}
