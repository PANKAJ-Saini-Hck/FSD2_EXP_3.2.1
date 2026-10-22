package com.experiment.productservice.testcontainers;

import com.experiment.productservice.model.Product;
import com.experiment.productservice.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ─────────────────────────────────────────────────────────────────
 * INFRASTRUCTURE TESTS  –  ProductRepository (Testcontainers)
 * ─────────────────────────────────────────────────────────────────
 * Testing Level : Infrastructure / Integration
 * Framework     : JUnit 5 + Testcontainers + Spring Data JPA
 * Database      : Real PostgreSQL 15 running inside Docker container
 *
 * Testcontainers spins up a fresh PostgreSQL instance before the
 * test class runs and tears it down afterwards.  Spring Boot's
 * {@link DynamicPropertySource} injects the container's JDBC URL,
 * username and password at runtime — no hard-coded config needed.
 *
 * This ensures tests run against the same DB engine as production,
 * catching SQL dialect issues that H2 would miss.
 * ─────────────────────────────────────────────────────────────────
 */
@SpringBootTest
@Testcontainers
@DisplayName("Infrastructure Tests – ProductRepository (Testcontainers + PostgreSQL)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductRepositoryIT {

    // ── PostgreSQL Container ───────────────────────────────────────────────

    /**
     * Shared PostgreSQL 15 container.
     * The {@code @Container} annotation tells Testcontainers to manage
     * its lifecycle (start/stop) automatically.
     */
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("productdb_test")
                    .withUsername("testuser")
                    .withPassword("testpass");

    /**
     * Injects the dynamically assigned container coordinates into the
     * Spring application context before beans are created.
     */
    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name",
                     () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform",
                     () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    // ── Autowired Dependencies ────────────────────────────────────────────

    @Autowired
    private ProductRepository productRepository;

    // ── Setup ─────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Product save(String name, String price, String category, int stock) {
        return productRepository.save(
                new Product(name, new BigDecimal(price), category, stock));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  1.  Container Sanity Check
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("PostgreSQL container should be running")
    void containerShouldBeRunning() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.getJdbcUrl()).contains("postgresql");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  2.  Basic CRUD against real PostgreSQL
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("should save and retrieve product from PostgreSQL")
    void shouldSaveAndRetrieveProduct() {
        Product saved = save("Gaming Chair", "299.99", "Furniture", 20);

        assertThat(saved.getId()).isNotNull();

        Product found = productRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Gaming Chair");
        assertThat(found.getPrice()).isEqualByComparingTo("299.99");
        assertThat(found.getCategory()).isEqualTo("Furniture");
        assertThat(found.getStock()).isEqualTo(20);
    }

    @Test
    @Order(3)
    @DisplayName("should persist multiple products and count them correctly")
    void shouldPersistMultipleProducts() {
        save("Laptop",  "999.99",  "Electronics", 50);
        save("Tablet",  "499.99",  "Electronics", 30);
        save("Monitor", "349.99",  "Electronics", 25);

        long count = productRepository.count();
        assertThat(count).isEqualTo(3);
    }

    @Test
    @Order(4)
    @DisplayName("should update a product field correctly in PostgreSQL")
    void shouldUpdateProduct() {
        Product p = save("Old Mouse", "29.99", "Peripherals", 100);
        p.setName("New Mouse");
        p.setPrice(new BigDecimal("39.99"));
        productRepository.save(p);

        Product updated = productRepository.findById(p.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Mouse");
        assertThat(updated.getPrice()).isEqualByComparingTo("39.99");
    }

    @Test
    @Order(5)
    @DisplayName("should delete a product from PostgreSQL")
    void shouldDeleteProduct() {
        Product p = save("Keyboard", "79.99", "Peripherals", 150);
        Long id = p.getId();

        productRepository.deleteById(id);

        assertThat(productRepository.findById(id)).isEmpty();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  3.  Custom Finder Methods
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("findByCategory() should return only products in that category")
    void shouldFindByCategory() {
        save("Laptop",    "999.99", "Electronics", 50);
        save("Headphones","149.99", "Electronics", 100);
        save("Desk",      "249.99", "Furniture",   10);

        List<Product> electronics = productRepository.findByCategory("Electronics");

        assertThat(electronics)
                .hasSize(2)
                .allMatch(p -> p.getCategory().equals("Electronics"));
    }

    @Test
    @Order(7)
    @DisplayName("findByNameContainingIgnoreCase() should do case-insensitive search")
    void shouldSearchByNameCaseInsensitive() {
        save("Laptop Pro",   "1299.99", "Electronics", 20);
        save("Laptop Air",   "999.99",  "Electronics", 30);
        save("Desktop Tower","799.99",  "Electronics", 15);

        List<Product> results = productRepository.findByNameContainingIgnoreCase("LAPTOP");

        assertThat(results)
                .hasSize(2)
                .allMatch(p -> p.getName().toLowerCase().contains("laptop"));
    }

    @Test
    @Order(8)
    @DisplayName("findInStockProducts() should only return products with stock > 0")
    void shouldFindOnlyInStockProducts() {
        save("In Stock Item",   "100.00", "Test", 10);
        save("Out of Stock",    "50.00",  "Test", 0);

        List<Product> inStock = productRepository.findInStockProducts();

        assertThat(inStock)
                .hasSize(1)
                .allMatch(p -> p.getStock() > 0);
        assertThat(inStock.get(0).getName()).isEqualTo("In Stock Item");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  4.  JPA Constraint Validation via DB
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @Order(9)
    @DisplayName("should return empty Optional for non-existent product id")
    void shouldReturnEmptyForNonExistentId() {
        assertThat(productRepository.findById(99999L)).isEmpty();
    }
}
