package com.experiment.orderservice.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.experiment.orderservice.client.ProductClient;
import com.experiment.orderservice.model.Product;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * ─────────────────────────────────────────────────────────────────
 * CONTRACT TESTS (CONSUMER)  –  ProductConsumerPactTest
 * ─────────────────────────────────────────────────────────────────
 * Testing Level : Contract (Consumer side)
 * Framework     : Pact JVM 4.x – Consumer-Driven Contract Testing
 *
 * HOW IT WORKS:
 *  1. This class defines INTERACTIONS (contracts) the Order Service
 *     (consumer) expects from the Product Service (provider).
 *  2. Pact starts a mock HTTP server acting as the real Product Service.
 *  3. ProductClient calls this mock server.
 *  4. If the client handles the mock responses correctly, Pact writes
 *     a Pact JSON file to target/pacts/.
 *  5. The Product Service (provider) verifies it satisfies every
 *     interaction defined in that Pact file.
 *
 * Pacts defined:
 *  - GET /api/products           → list of all products
 *  - GET /api/products/1         → single product (found)
 *  - GET /api/products/9999      → 404 (not found)
 *  - GET /api/products/category/ → filtered list by category
 * ─────────────────────────────────────────────────────────────────
 */
@ExtendWith(PactConsumerTestExt.class)
@DisplayName("Contract Tests (Consumer) – Order Service → Product Service")
class ProductConsumerPactTest {

    private static final String CONSUMER = "order-service";
    private static final String PROVIDER = "product-service";

    // ══════════════════════════════════════════════════════════════════════
    //  PACT 1 – GET all products → returns list
    // ══════════════════════════════════════════════════════════════════════

    @Pact(consumer = CONSUMER, provider = PROVIDER)
    public RequestResponsePact getAllProductsPact(PactDslWithProvider builder) {
        return builder
                .given("products exist")
                .uponReceiving("a request to get all products")
                    .method("GET")
                    .path("/api/products")
                .willRespondWith()
                    .status(200)
                    .headers(Map.of("Content-Type", "application/json"))
                    // Use LambdaDsl for correct array-of-objects DSL in Pact 4.x
                    .body(LambdaDsl.newJsonArrayMinLike(1, arr ->
                        arr.object(o -> {
                            o.numberType("id", 1L);
                            o.stringType("name", "Laptop");
                            o.decimalType("price", 999.99);
                            o.stringType("category", "Electronics");
                            o.integerType("stock", 50);
                        })
                    ).build())
                .toPact();
    }

    @Test
    @PactTestFor(providerName = PROVIDER, pactMethod = "getAllProductsPact")
    @DisplayName("Consumer: GET /api/products should return a list of products")
    void testGetAllProducts(MockServer mockServer) {
        ProductClient client = buildClient(mockServer);

        List<Product> products = client.getAllProducts();

        assertThat(products).isNotEmpty();
        assertThat(products.get(0).getName()).isNotBlank();
        assertThat(products.get(0).getPrice()).isPositive();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PACT 2 – GET /api/products/{id} → found
    // ══════════════════════════════════════════════════════════════════════

    @Pact(consumer = CONSUMER, provider = PROVIDER)
    public RequestResponsePact getProductByIdFoundPact(PactDslWithProvider builder) {
        return builder
                .given("product with id 1 exists")
                .uponReceiving("a request to get product with id 1")
                    .method("GET")
                    .path("/api/products/1")
                .willRespondWith()
                    .status(200)
                    .headers(Map.of("Content-Type", "application/json"))
                    // Use type-matching for id so any auto-generated id passes
                    .body(LambdaDsl.newJsonBody(o -> {
                        o.numberType("id", 1L);
                        o.stringValue("name", "Laptop");
                        o.decimalType("price", 999.99);
                        o.stringValue("category", "Electronics");
                        o.integerType("stock", 50);
                    }).build())
                .toPact();
    }

    @Test
    @PactTestFor(providerName = PROVIDER, pactMethod = "getProductByIdFoundPact")
    @DisplayName("Consumer: GET /api/products/1 should return a product when it exists")
    void testGetProductByIdFound(MockServer mockServer) {
        ProductClient client = buildClient(mockServer);

        Optional<Product> product = client.getProductById(1L);

        assertThat(product).isPresent();
        assertThat(product.get().getName()).isEqualTo("Laptop");
        assertThat(product.get().getCategory()).isEqualTo("Electronics");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PACT 3 – GET /api/products/{id} → not found (404)
    // ══════════════════════════════════════════════════════════════════════

    @Pact(consumer = CONSUMER, provider = PROVIDER)
    public RequestResponsePact getProductByIdNotFoundPact(PactDslWithProvider builder) {
        return builder
                .given("product with id 9999 does not exist")
                .uponReceiving("a request to get product with id 9999")
                    .method("GET")
                    .path("/api/products/9999")
                .willRespondWith()
                    .status(404)
                .toPact();
    }

    @Test
    @PactTestFor(providerName = PROVIDER, pactMethod = "getProductByIdNotFoundPact")
    @DisplayName("Consumer: GET /api/products/9999 should return empty when not found")
    void testGetProductByIdNotFound(MockServer mockServer) {
        ProductClient client = buildClient(mockServer);

        Optional<Product> product = client.getProductById(9999L);

        assertThat(product).isEmpty();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PACT 4 – GET /api/products/category/{category}
    // ══════════════════════════════════════════════════════════════════════

    @Pact(consumer = CONSUMER, provider = PROVIDER)
    public RequestResponsePact getProductsByCategoryPact(PactDslWithProvider builder) {
        return builder
                .given("electronics products exist")
                .uponReceiving("a request to get products in Electronics category")
                    .method("GET")
                    .path("/api/products/category/Electronics")
                .willRespondWith()
                    .status(200)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(LambdaDsl.newJsonArrayMinLike(1, arr ->
                        arr.object(o -> {
                            o.numberType("id", 1L);
                            o.stringType("name", "Laptop");
                            o.decimalType("price", 999.99);
                            o.stringValue("category", "Electronics");
                            o.integerType("stock", 50);
                        })
                    ).build())
                .toPact();
    }

    @Test
    @PactTestFor(providerName = PROVIDER, pactMethod = "getProductsByCategoryPact")
    @DisplayName("Consumer: GET /api/products/category/Electronics should return electronics")
    void testGetProductsByCategory(MockServer mockServer) {
        ProductClient client = buildClient(mockServer);

        List<Product> electronics = client.getProductsByCategory("Electronics");

        assertThat(electronics).isNotEmpty();
        assertThat(electronics).allMatch(p -> "Electronics".equals(p.getCategory()));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private ProductClient buildClient(MockServer mockServer) {
        return new ProductClient(new RestTemplate(), mockServer.getUrl());
    }
}
