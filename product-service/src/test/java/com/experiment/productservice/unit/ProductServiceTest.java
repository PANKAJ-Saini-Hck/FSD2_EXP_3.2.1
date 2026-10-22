package com.experiment.productservice.unit;

import com.experiment.productservice.model.Product;
import com.experiment.productservice.repository.ProductRepository;
import com.experiment.productservice.service.ProductService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ─────────────────────────────────────────────────────────────────
 * UNIT TESTS  –  ProductService
 * ─────────────────────────────────────────────────────────────────
 * Testing Level : Unit
 * Framework     : JUnit 5 (Jupiter) + Mockito
 * Strategy      : The repository is mocked; only the service logic
 *                 under test is exercised in isolation.
 *
 * Tests cover:
 *  - Happy-path CRUD operations
 *  - Business-rule: duplicate product name rejection
 *  - Error paths: product not found on update / delete
 * ─────────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests – ProductService")
class ProductServiceTest {

    // ── Mocks & Subject Under Test ────────────────────────────────────────

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    // ── Test Data Helpers ─────────────────────────────────────────────────

    private Product buildProduct(Long id, String name, BigDecimal price,
                                  String category, int stock) {
        Product p = new Product(name, price, category, stock);
        p.setId(id);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  1.  getAllProducts()
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAllProducts()")
    class GetAllProducts {

        @Test
        @DisplayName("should return all products from the repository")
        void shouldReturnAllProducts() {
            // Arrange
            List<Product> expected = List.of(
                    buildProduct(1L, "Laptop",     new BigDecimal("999.99"),  "Electronics", 50),
                    buildProduct(2L, "Headphones", new BigDecimal("149.99"),  "Electronics", 200)
            );
            when(productRepository.findAll()).thenReturn(expected);

            // Act
            List<Product> result = productService.getAllProducts();

            // Assert
            assertThat(result)
                    .hasSize(2)
                    .extracting(Product::getName)
                    .containsExactly("Laptop", "Headphones");

            verify(productRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("should return empty list when no products exist")
        void shouldReturnEmptyListWhenNoProducts() {
            when(productRepository.findAll()).thenReturn(Collections.emptyList());

            List<Product> result = productService.getAllProducts();

            assertThat(result).isEmpty();
            verify(productRepository).findAll();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  2.  getProductById()
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getProductById()")
    class GetProductById {

        @Test
        @DisplayName("should return product when it exists")
        void shouldReturnProductWhenExists() {
            Product product = buildProduct(1L, "Laptop", new BigDecimal("999.99"), "Electronics", 50);
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            Optional<Product> result = productService.getProductById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Laptop");
            assertThat(result.get().getPrice()).isEqualByComparingTo("999.99");
        }

        @Test
        @DisplayName("should return empty Optional when product does not exist")
        void shouldReturnEmptyWhenNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Product> result = productService.getProductById(99L);

            assertThat(result).isEmpty();
            verify(productRepository).findById(99L);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  3.  createProduct()
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createProduct()")
    class CreateProduct {

        @Test
        @DisplayName("should save and return the product when name is unique")
        void shouldSaveProductWithUniqueName() {
            Product input = new Product("Smartphone", new BigDecimal("699.99"), "Electronics", 100);
            Product saved = buildProduct(1L, "Smartphone", new BigDecimal("699.99"), "Electronics", 100);

            // No existing product with the same name
            when(productRepository.findByNameContainingIgnoreCase("Smartphone"))
                    .thenReturn(Collections.emptyList());
            when(productRepository.save(input)).thenReturn(saved);

            Product result = productService.createProduct(input);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Smartphone");
            verify(productRepository).save(input);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when product name already exists")
        void shouldThrowWhenDuplicateName() {
            Product existing = buildProduct(1L, "Laptop", new BigDecimal("999.99"), "Electronics", 50);
            Product duplicate = new Product("Laptop", new BigDecimal("888.00"), "Electronics", 10);

            when(productRepository.findByNameContainingIgnoreCase("Laptop"))
                    .thenReturn(List.of(existing));

            assertThatThrownBy(() -> productService.createProduct(duplicate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");

            // Save should NEVER be called if duplicate
            verify(productRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  4.  updateProduct()
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateProduct()")
    class UpdateProduct {

        @Test
        @DisplayName("should update fields and return the updated product")
        void shouldUpdateProductFields() {
            Product existing = buildProduct(1L, "Old Name", new BigDecimal("100.00"), "Cat A", 10);
            Product updated  = new Product("New Name", new BigDecimal("200.00"), "Cat B", 20);

            when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            Product result = productService.updateProduct(1L, updated);

            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getPrice()).isEqualByComparingTo("200.00");
            assertThat(result.getCategory()).isEqualTo("Cat B");
            assertThat(result.getStock()).isEqualTo(20);
        }

        @Test
        @DisplayName("should throw NoSuchElementException when product not found")
        void shouldThrowWhenProductNotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());
            Product update = new Product("X", BigDecimal.ONE, "Y", 1);

            assertThatThrownBy(() -> productService.updateProduct(999L, update))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("999");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  5.  deleteProduct()
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProduct {

        @Test
        @DisplayName("should call repository deleteById when product exists")
        void shouldDeleteExistingProduct() {
            when(productRepository.existsById(1L)).thenReturn(true);

            productService.deleteProduct(1L);

            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw NoSuchElementException when product does not exist")
        void shouldThrowWhenDeletingNonExistentProduct() {
            when(productRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("999");

            verify(productRepository, never()).deleteById(anyLong());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  6.  getProductsByCategory() & searchByName()
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Filter & Search")
    class FilterAndSearch {

        @Test
        @DisplayName("getProductsByCategory() should delegate to repository")
        void shouldReturnProductsByCategory() {
            List<Product> electronics = List.of(
                    buildProduct(1L, "Laptop", new BigDecimal("999.99"), "Electronics", 50));
            when(productRepository.findByCategory("Electronics")).thenReturn(electronics);

            List<Product> result = productService.getProductsByCategory("Electronics");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("searchByName() should return matching products case-insensitively")
        void shouldSearchByNameKeyword() {
            List<Product> matches = List.of(
                    buildProduct(1L, "Laptop Pro", new BigDecimal("1299.99"), "Electronics", 30));
            when(productRepository.findByNameContainingIgnoreCase("laptop")).thenReturn(matches);

            List<Product> result = productService.searchByName("laptop");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).containsIgnoringCase("laptop");
        }

        @Test
        @DisplayName("getInStockProducts() should only return products with stock > 0")
        void shouldReturnOnlyInStockProducts() {
            List<Product> inStock = List.of(
                    buildProduct(1L, "Tablet", new BigDecimal("499.99"), "Electronics", 5));
            when(productRepository.findInStockProducts()).thenReturn(inStock);

            List<Product> result = productService.getInStockProducts();

            assertThat(result).allMatch(p -> p.getStock() > 0);
        }
    }
}
