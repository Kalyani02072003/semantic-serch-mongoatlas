package com.demo.semantic_search;

import com.demo.semantic_search.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SemanticSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SemanticSearchApplication.class, args);
	}

}
