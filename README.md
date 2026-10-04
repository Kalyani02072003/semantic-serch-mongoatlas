## API Usage

The application exposes APIs for experimenting with embeddings locally using **Ollama** and performing semantic search using **MongoDB Atlas Vector Search**.

### Architecture

```text
                         ┌─────────────────────┐
                         │     Spring Boot     │
                         └──────────┬──────────┘
                                    │
                   ┌────────────────┴────────────────┐
                   │                                 │
                   ▼                                 ▼
          LOCAL OLLAMA PATH                 MONGODB ATLAS PATH
                   │                                 │
                   ▼                                 ▼
          nomic-embed-text              Atlas Automated Embedding
                   │                                 │
                   ▼                                 ▼
              Embedding                         Vector Search
                   │                                 │
                   ▼                                 ▼
             Vector output                    Ranked results
```

---

## 1. Local Ollama — Generate an Embedding

**Endpoint:** `GET /embedding`

This endpoint demonstrates how text is converted into a numerical vector using the local **Ollama `nomic-embed-text`** embedding model.

It is primarily intended for understanding what happens before semantic search.

### Request

```bash
curl --get "http://localhost:8080/embedding" \
  --data-urlencode "text=Waterproof hiking jacket"
```

You can also try a natural-language query:

```bash
curl --get "http://localhost:8080/embedding" \
  --data-urlencode "text=something to wear when hiking in rain"
```

### What happens?

```text
Text
  │
  ▼
Ollama
  │
  ▼
nomic-embed-text
  │
  ▼
Embedding vector
```

The model converts the input text into a fixed-length numerical representation.

For example, conceptually:

```text
"Waterproof hiking jacket"

        ↓

[0.02, -0.18, 0.71, ...]
```

The actual embedding contains many dimensions; the numbers themselves are not human-readable labels. Their purpose is to represent the semantic characteristics of the text.

---

# 2. Products API

**Endpoint:** `GET /products`

This endpoint returns the products stored in the application's `products` collection.

### Request

```bash
curl http://localhost:8080/products
```

The product documents contain fields such as:

```json
{
  "name": "Waterproof Hiking Jacket",
  "description": "A lightweight waterproof jacket designed for hiking in rainy weather.",
  "category": "Outdoor"
}
```

These product descriptions are the text that is used for semantic retrieval.

The MongoDB structure used in the project is:

```text
semantic_search
└── products
    ├── name
    ├── description
    └── category
```

---

# 3. MongoDB Atlas — Semantic Search

**Endpoint:** `GET /search`

This is the main semantic-search endpoint.

Unlike `/embedding`, this endpoint does not simply return an embedding vector. It performs a semantic search against the product data using **MongoDB Atlas Vector Search**.

### Request

```bash
curl --get "http://localhost:8080/search" \
  --data-urlencode "q=something to wear when hiking in rain"
```

### Example queries

#### Hiking / rain

```bash
curl --get "http://localhost:8080/search" \
  --data-urlencode "q=something to wear when hiking in rain"
```

This can retrieve:

```text
Waterproof Hiking Jacket
```

even though the query does not explicitly contain the word `jacket`.

---

#### Camping / cold weather

```bash
curl --get "http://localhost:8080/search" \
  --data-urlencode "q=keep me warm while sleeping outside"
```

This can retrieve semantically related products such as:

```text
Insulated Camping Blanket
Cold Weather Sleeping Bag
Thermal Base Layer
```

---

#### Running / mountains

```bash
curl --get "http://localhost:8080/search" \
  --data-urlencode "q=shoes for running on rocky mountain paths"
```

This can retrieve:

```text
Trail Running Shoes
Mountain Hiking Boots
```

---

#### Another semantic query

```bash
curl --get "http://localhost:8080/search" \
  --data-urlencode "q=protect me from the rain"
```

The search returns products ranked according to their vector similarity.

---

## How `/search` works

The high-level flow is:

```text
User query
    │
    ▼
Spring Boot
    │
    ▼
MongoDB Atlas Vector Search
    │
    ├── Generate / use vector representation
    │
    ├── Compare against indexed vectors
    │
    ├── Rank by similarity
    │
    ▼
Top-K products
```

The MongoDB operation used by the application is based on `$vectorSearch`:

```javascript
{
  $vectorSearch: {
    index: "semantic_product_search",
    query: {
      text: "something to wear when hiking in rainy weather"
    },
    path: "description",
    numCandidates: 100,
    limit: 10
  }
}
```

The vector search index used in the project is:

```text
Index: semantic_product_search
Field: description
Embedding model: voyage-4
```

---

# 4. Local Ollama vs MongoDB Atlas

The two APIs demonstrate two different parts of the semantic-search pipeline.

| Endpoint         | Technology                  | Purpose                                              |
| ---------------- | --------------------------- | ---------------------------------------------------- |
| `GET /embedding` | Ollama + `nomic-embed-text` | Generate and inspect embeddings locally              |
| `GET /products`  | Spring Boot + MongoDB       | View stored product data                             |
| `GET /search`    | MongoDB Atlas Vector Search | Perform semantic search and retrieve ranked products |

### Local embedding path

```text
Text
  │
  ▼
Ollama
  │
  ▼
nomic-embed-text
  │
  ▼
Vector
```

The `/embedding` endpoint lets us see this part of the system directly.

### Atlas semantic-search path

```text
Query
  │
  ▼
Embedding
  │
  ▼
MongoDB Atlas Vector Search
  │
  ▼
Similarity / nearest-neighbor search
  │
  ▼
Ranked results
```

The `/search` endpoint demonstrates the complete retrieval workflow.

---

## 5. The Mathematics Behind `/search`

At a conceptual level, semantic search works by representing both the query and documents as vectors:

```text
Query → q = [q₁, q₂, ..., qᵈ]

Document → d = [d₁, d₂, ..., dᵈ]
```

A similarity function can then be used to compare them.

For cosine similarity:

```text
                    q · d
cos(q, d) = ───────────────────
             ||q|| × ||d||
```

The search process can therefore be viewed as:

```text
1. Convert the query into a vector
2. Compare it with document vectors
3. Calculate similarity
4. Rank the documents
5. Return the top-K results
```

This turns semantic search into a **nearest-neighbor search problem in a high-dimensional vector space**.

For large datasets, vector indexes such as **HNSW** can be used to make nearest-neighbor retrieval more efficient than comparing against every stored vector.

---

## 6. Running the Application

Start the Spring Boot application and make sure it is listening on:

```text
http://localhost:8080
```

Make sure Ollama is running locally for the embedding endpoint:

```bash
ollama pull nomic-embed-text
```

Then test the local embedding API:

```bash
curl --get "http://localhost:8080/embedding" \
  --data-urlencode "text=Waterproof hiking jacket"
```

And test MongoDB Atlas semantic search:

```bash
curl --get "http://localhost:8080/search" \
  --data-urlencode "q=something to wear when hiking in rain"
```

---

## Summary

This project demonstrates semantic search at two levels:

**Local understanding**

```text
Text → Ollama → Embedding Vector
```

**Production-style retrieval**

```text
Text → Embedding → MongoDB Atlas Vector Search → Ranked Results
```

The key idea is:

> **The embedding model turns meaning into numbers. Vector search makes those numbers searchable.**
