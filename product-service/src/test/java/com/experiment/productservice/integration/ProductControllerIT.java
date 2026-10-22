package com.experiment.productservice.integration;

import com.experiment.productservice.model.Product;
import com.experiment.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ─────────────────────────────────────────────────────────────────
 * INTEGRATION TESTS  –  ProductController (MockMvc)
 * ─────────────────────────────────────────────────────────────────
 * Testing Level : Integration
 * Framework     : Spring Boot Test + MockMvc
 * Database      : H2 in-memory (via test profile)
 *
 * Loads the full Spring Application Context and exercises the
 * HTTP layer end-to-end: serialization → controller → service →
 * repository → H2 database → deserialization.
 *
 * No mocking of internal components — all beans are real.
 * ─────────────────────────────────────────────────────────────────
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")       // loads application-test.properties (H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Integration Tests – ProductController (MockMvc)")
class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    // ── Setup & Teardown ──────────────────────────────────────────────────

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Product savedProduct(String name, String price, String category, int stock) {
        Product p = new Product(name, new BigDecimal(price), category, stock);
        return productRepository.save(p);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  1.  GET /api/products
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("GET /api/products – should return empty list when no products")
    void getAll_Empty() throws Exception {
        mockMvc.perform(get("/api/products"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/products – should return all products")
    void getAll_WithProducts() throws Exception {
        savedProduct("Laptop",  "999.99",  "Electronics", 50);
        savedProduct("Monitor", "349.99",  "Electronics", 25);

        mockMvc.perform(get("/api/products"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0].name", is("Laptop")))
               .andExpect(jsonPath("$[1].name", is("Monitor")));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  2.  GET /api/products/{id}
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("GET /api/products/{id} – should return 200 for existing product")
    void getById_Found() throws Exception {
        Product saved = savedProduct("Keyboard", "79.99", "Peripherals", 150);

        mockMvc.perform(get("/api/products/{id}", saved.getId()))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id",       is(saved.getId().intValue())))
               .andExpect(jsonPath("$.name",     is("Keyboard")))
               .andExpect(jsonPath("$.price",    is(79.99)))
               .andExpect(jsonPath("$.category", is("Peripherals")))
               .andExpect(jsonPath("$.stock",    is(150)));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/products/{id} – should return 404 for unknown id")
    void getById_NotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 99999L))
               .andDo(print())
               .andExpect(status().isNotFound());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  3.  POST /api/products
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("POST /api/products – should create product and return 201")
    void createProduct_Success() throws Exception {
        Product newProduct = new Product("Webcam", new BigDecimal("89.99"), "Peripherals", 75);

        mockMvc.perform(post("/api/products")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(toJson(newProduct)))
               .andDo(print())
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id",       notNullValue()))
               .andExpect(jsonPath("$.name",     is("Webcam")))
               .andExpect(jsonPath("$.price",    is(89.99)))
               .andExpect(jsonPath("$.stock",    is(75)));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/products – should return 409 for duplicate product name")
    void createProduct_DuplicateName_Conflict() throws Exception {
        savedProduct("Laptop", "999.99", "Electronics", 50);

        Product duplicate = new Product("Laptop", new BigDecimal("888.00"), "Electronics", 10);

        mockMvc.perform(post("/api/products")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(toJson(duplicate)))
               .andDo(print())
               .andExpect(status().isConflict());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/products – should return 400 for invalid (blank name)")
    void createProduct_InvalidInput() throws Exception {
        // blank name violates @NotBlank constraint
        String invalidJson = "{\"name\":\"\",\"price\":100.00,\"category\":\"Test\",\"stock\":10}";

        mockMvc.perform(post("/api/products")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(invalidJson))
               .andDo(print())
               .andExpect(status().isBadRequest());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  4.  PUT /api/products/{id}
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(8)
    @DisplayName("PUT /api/products/{id} – should update and return updated product")
    void updateProduct_Success() throws Exception {
        Product saved = savedProduct("Old Mouse", "29.99", "Peripherals", 200);
        Product update = new Product("New Mouse", new BigDecimal("39.99"), "Peripherals", 180);

        mockMvc.perform(put("/api/products/{id}", saved.getId())
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(toJson(update)))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name",  is("New Mouse")))
               .andExpect(jsonPath("$.price", is(39.99)))
               .andExpect(jsonPath("$.stock", is(180)));
    }

    @Test
    @Order(9)
    @DisplayName("PUT /api/products/{id} – should return 404 for unknown id")
    void updateProduct_NotFound() throws Exception {
        Product update = new Product("X", new BigDecimal("1.00"), "Y", 1);

        mockMvc.perform(put("/api/products/{id}", 99999L)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(toJson(update)))
               .andDo(print())
               .andExpect(status().isNotFound());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  5.  DELETE /api/products/{id}
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("DELETE /api/products/{id} – should return 204 for existing product")
    void deleteProduct_Success() throws Exception {
        Product saved = savedProduct("Printer", "199.99", "Office", 30);

        mockMvc.perform(delete("/api/products/{id}", saved.getId()))
               .andDo(print())
               .andExpect(status().isNoContent());

        // Confirm it's gone
        mockMvc.perform(get("/api/products/{id}", saved.getId()))
               .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @DisplayName("DELETE /api/products/{id} – should return 404 for unknown id")
    void deleteProduct_NotFound() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 99999L))
               .andDo(print())
               .andExpect(status().isNotFound());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  6.  GET /api/products/category/{category}
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(12)
    @DisplayName("GET /api/products/category/{category} – should filter by category")
    void getByCategory() throws Exception {
        savedProduct("Laptop",     "999.99", "Electronics",  50);
        savedProduct("Headphones", "149.99", "Electronics",  200);
        savedProduct("Desk",       "249.99", "Furniture",    10);

        mockMvc.perform(get("/api/products/category/{category}", "Electronics"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[*].category", everyItem(is("Electronics"))));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  7.  GET /api/products/search?q=
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(13)
    @DisplayName("GET /api/products/search?q= – should return case-insensitive matches")
    void searchByName() throws Exception {
        savedProduct("Laptop Pro",    "1299.99", "Electronics", 20);
        savedProduct("Laptop Air",    "999.99",  "Electronics", 30);
        savedProduct("Desktop Tower", "799.99",  "Electronics", 15);

        mockMvc.perform(get("/api/products/search").param("q", "laptop"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[*].name",
                       everyItem(containsStringIgnoringCase("laptop"))));
    }
}
