package com.demo.semantic_search.repository;

import com.demo.semantic_search.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
