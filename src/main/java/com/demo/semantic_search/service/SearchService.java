package com.demo.semantic_search.service;

import com.demo.semantic_search.dto.SearchResult;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final MongoTemplate mongoTemplate;

    public SearchService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<SearchResult> search(String query) {

        Document vectorSearch = new Document(
                "$vectorSearch",
                new Document("index", "semantic_product_search")
                        .append("path", "description")
                        .append("query", new Document("text", query))
                        .append("numCandidates", 100)
                        .append("limit", 10)
        );

        Document project = new Document(
                "$project",
                new Document("_id", 0)
                        .append("name", 1)
                        .append("description", 1)
                        .append("category", 1)
                        .append("score",
                                new Document("$meta", "vectorSearchScore"))
        );

        List<Document> documents = mongoTemplate
                .getCollection("products")
                .aggregate(List.of(vectorSearch, project))
                .into(new ArrayList<>());

        List<SearchResult> results = new ArrayList<>();

        for (Document document : documents) {

            results.add(
                    new SearchResult(
                            document.getString("name"),
                            document.getString("description"),
                            document.getString("category"),
                            document.getDouble("score")
                    )
            );
        }

        return results;
    }
}