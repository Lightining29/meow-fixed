package com.login.web;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Categoryrepository extends MongoRepository<Cat, Integer> {
}
