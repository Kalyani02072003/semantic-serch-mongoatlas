package com.demo.semantic_search.controller;

import com.demo.semantic_search.Product;
import com.demo.semantic_search.repository.ProductRepository;
import com.demo.semantic_search.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProductController(ProductRepository productRepository, ProductService productService){
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    @GetMapping
    public List<Product> getProducts(){
        return productRepository.findAll();
    }

}
