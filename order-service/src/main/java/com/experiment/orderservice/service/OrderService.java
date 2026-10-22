package com.experiment.orderservice.service;

import com.experiment.orderservice.client.ProductClient;
import com.experiment.orderservice.model.Order;
import com.experiment.orderservice.model.Product;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/**
 * Business logic for creating and managing orders.
 *
 * <p>Depends on {@link ProductClient} to look up product details
 * from the Product Service before placing an order.
 */
@Service
public class OrderService {

    private final ProductClient productClient;

    public OrderService(ProductClient productClient) {
        this.productClient = productClient;
    }

    /**
     * Places a new order for the specified product and quantity.
     *
     * <p>Validates that:
     * <ul>
     *   <li>The product exists</li>
     *   <li>Sufficient stock is available</li>
     *   <li>Quantity is positive</li>
     * </ul>
     *
     * @param productId the ID of the product to order
     * @param quantity  the quantity to order (must be &gt; 0)
     * @return the created {@link Order}
     * @throws IllegalArgumentException  if quantity &le; 0
     * @throws NoSuchElementException    if product is not found
     * @throws IllegalStateException     if stock is insufficient
     */
    public Order placeOrder(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Order quantity must be greater than zero, got: " + quantity);
        }

        Product product = productClient.getProductById(productId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Product not found with id: " + productId));

        if (product.getStock() < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock. Requested: " + quantity
                    + ", Available: " + product.getStock());
        }

        return new Order(
                product.getId(),
                product.getName(),
                quantity,
                product.getPrice()
        );
    }
}
