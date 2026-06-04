package com.login.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebApplicationTests {

    @Autowired
    private Categoryrepository categoryRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void testCatAutoIncrementId() {
        // 1. Create and save first category
        Cat cat1 = new Cat();
        cat1.setCategory("Test Category 1");
        Cat savedCat1 = categoryRepository.save(cat1);

        assertNotNull(savedCat1);
        assertTrue(savedCat1.getId() > 0, "ID should be auto-incremented to a value greater than 0");

        // 2. Create and save second category
        Cat cat2 = new Cat();
        cat2.setCategory("Test Category 2");
        Cat savedCat2 = categoryRepository.save(cat2);

        assertNotNull(savedCat2);
        assertEquals(savedCat1.getId() + 1, savedCat2.getId(), "Next ID should be incremented by 1");

        // Cleanup
        categoryRepository.delete(savedCat1);
        categoryRepository.delete(savedCat2);
    }
}
