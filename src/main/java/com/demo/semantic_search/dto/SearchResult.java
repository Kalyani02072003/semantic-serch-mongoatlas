package com.demo.semantic_search.dto;

public class SearchResult {

    private String name;
    private String description;
    private String category;
    private Double score;

    public SearchResult(
            String name,
            String description,
            String category,
            Double score
    ) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Double getScore() {
        return score;
    }
}