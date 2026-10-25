package com.experiment.orderservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a customer order in the Order Service.
 *
 * <p>An order references a product by ID and records the quantity and
 * total price at the time of ordering.
 */
public class Order {

    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String status;          // e.g. PENDING, CONFIRMED, CANCELLED
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────────

    public Order() {}

    public Order(Long productId, String productName, int quantity,
                 BigDecimal unitPrice) {
        this.productId   = productId;
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
        this.totalPrice  = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.status      = "PENDING";
        this.createdAt   = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public Long getProductId()                   { return productId; }
    public void setProductId(Long pid)           { this.productId = pid; }

    public String getProductName()               { return productName; }
    public void setProductName(String name)      { this.productName = name; }

    public int getQuantity()                     { return quantity; }
    public void setQuantity(int qty)             { this.quantity = qty; }

    public BigDecimal getUnitPrice()             { return unitPrice; }
    public void setUnitPrice(BigDecimal up)      { this.unitPrice = up; }

    public BigDecimal getTotalPrice()            { return totalPrice; }
    public void setTotalPrice(BigDecimal tp)     { this.totalPrice = tp; }

    public String getStatus()                    { return status; }
    public void setStatus(String status)         { this.status = status; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }
}
