package com.experiment.productservice.controller;

import com.experiment.productservice.model.Product;
import com.experiment.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST Controller exposing the Product API.
 *
 * <p>This controller is tested at three levels:
 * <ul>
 *   <li><b>Unit</b>  – service layer mocked with Mockito</li>
 *   <li><b>Integration</b> – full Spring context with MockMvc + H2</li>
 *   <li><b>Contract</b> – Pact provider verification against consumer pact</li>
 * </ul>
 *
 * <h2>Endpoints</h2>
 * <pre>
 * GET    /api/products           → list all products
 * GET    /api/products/{id}      → get product by id
 * GET    /api/products/category/{cat} → filter by category
 * GET    /api/products/search?q=  → search by name keyword
 * POST   /api/products           → create product
 * PUT    /api/products/{id}      → update product
 * DELETE /api/products/{id}      → delete product
 * </pre>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ── GET all ──────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // ── GET by id ─────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET by category ───────────────────────────────────────────────────

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    // ── GET search by name ────────────────────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam("q") String keyword) {
        return ResponseEntity.ok(productService.searchByName(keyword));
    }

    // ── POST create ───────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        try {
            Product created = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ── PUT update ────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        try {
            Product updated = productService.updateProduct(id, product);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
