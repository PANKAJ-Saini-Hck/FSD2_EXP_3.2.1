package com.experiment.orderservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a product as seen by the Order Service (consumer).
 *
 * <p>This DTO mirrors the fields the Order Service expects from the
 * Product Service API. The Pact consumer test uses this model to define
 * the expected contract.
 */
public class Product {

    private Long id;
    private String name;
    private BigDecimal price;
    private String category;
    private int stock;

    // ── Constructors ──────────────────────────────────────────────────────

    public Product() {}

    public Product(Long id, String name, BigDecimal price, String category, int stock) {
        this.id       = id;
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
}
