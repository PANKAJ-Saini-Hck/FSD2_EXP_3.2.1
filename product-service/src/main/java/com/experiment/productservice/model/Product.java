package com.experiment.productservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * JPA Entity representing a Product in the catalogue.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code id}       – auto-generated primary key</li>
 *   <li>{@code name}     – product name (required, max 100 chars)</li>
 *   <li>{@code price}    – non-negative decimal price</li>
 *   <li>{@code category} – product category (e.g. "Electronics")</li>
 *   <li>{@code stock}    – available quantity (≥ 0)</li>
 * </ul>
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name must not be blank")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotBlank(message = "Category must not be blank")
    @Column(nullable = false)
    private String category;

    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private int stock;

    // ── Constructors ──────────────────────────────────────────────────────

    /** JPA no-arg constructor */
    protected Product() {}

    public Product(String name, BigDecimal price, String category, int stock) {
        this.name     = name;
        this.price    = price;
        this.category = category;
        this.stock    = stock;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public Long getId()                   { return id; }
    public void setId(Long id)            { this.id = id; }

    public String getName()               { return name; }
    public void setName(String name)      { this.name = name; }

    public BigDecimal getPrice()          { return price; }
    public void setPrice(BigDecimal p)    { this.price = p; }

    public String getCategory()           { return category; }
    public void setCategory(String cat)   { this.category = cat; }

    public int getStock()                 { return stock; }
    public void setStock(int stock)       { this.stock = stock; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price
                + ", category='" + category + "', stock=" + stock + '}';
    }
}
