package com.experiment.productservice.repository;

import com.experiment.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Product} entities.
 *
 * <p>Provides standard CRUD operations plus custom finder methods
 * used both by the application and by Testcontainers integration tests.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds all products belonging to the given category.
     *
     * @param category category name (case-sensitive)
     * @return list of matching products, may be empty
     */
    List<Product> findByCategory(String category);

    /**
     * Finds all products whose name contains the given keyword
     * (case-insensitive search).
     *
     * @param keyword search term
     * @return matching products
     */
    List<Product> findByNameContainingIgnoreCase(String keyword);

    /**
     * Returns products that are in stock (stock > 0).
     */
    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    List<Product> findInStockProducts();
}
