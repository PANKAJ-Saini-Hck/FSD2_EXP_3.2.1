package com.experiment.productservice.service;

import com.experiment.productservice.model.Product;
import com.experiment.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Product business logic.
 *
 * <p>All public methods are covered by <b>JUnit 5 unit tests</b> in
 * {@code ProductServiceTest} using Mockito to mock the repository.
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ── READ operations ───────────────────────────────────────────────────

    /**
     * Returns all products in the catalogue.
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Finds a product by its ID.
     *
     * @param id the product ID
     * @return an {@link Optional} containing the product, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Finds all products in a given category.
     *
     * @param category the category name
     * @return list of products in that category
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * Searches products by name keyword (case-insensitive).
     *
     * @param keyword search term
     * @return matching products
     */
    @Transactional(readOnly = true)
    public List<Product> searchByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * Returns all products currently in stock.
     */
    @Transactional(readOnly = true)
    public List<Product> getInStockProducts() {
        return productRepository.findInStockProducts();
    }

    // ── WRITE operations ──────────────────────────────────────────────────

    /**
     * Creates and persists a new product.
     *
     * @param product the product to save (id must be null)
     * @return the saved product with its generated id
     * @throws IllegalArgumentException if a product with the same name already exists
     */
    public Product createProduct(Product product) {
        // Business rule: product names must be unique
        boolean nameExists = productRepository
                .findByNameContainingIgnoreCase(product.getName())
                .stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(product.getName()));

        if (nameExists) {
            throw new IllegalArgumentException(
                    "A product with the name '" + product.getName() + "' already exists.");
        }

        return productRepository.save(product);
    }

    /**
     * Updates an existing product.
     *
     * @param id      the ID of the product to update
     * @param updated the updated product data
     * @return the updated product
     * @throws java.util.NoSuchElementException if no product with the given id exists
     */
    public Product updateProduct(Long id, Product updated) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Product not found with id: " + id));

        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        existing.setCategory(updated.getCategory());
        existing.setStock(updated.getStock());

        return productRepository.save(existing);
    }

    /**
     * Deletes a product by ID.
     *
     * @param id the product ID
     * @throws java.util.NoSuchElementException if no product with the given id exists
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new java.util.NoSuchElementException(
                    "Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
