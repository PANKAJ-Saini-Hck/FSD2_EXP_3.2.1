package com.experiment.orderservice.client;

import com.experiment.orderservice.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * HTTP client for communicating with the Product Service.
 *
 * <p>This is the <b>Pact consumer</b>: the interactions this client defines
 * (in {@code ProductConsumerPactTest}) describe the exact contract that the
 * Product Service must honour as the provider.
 */
@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String productServiceBaseUrl;

    @Autowired
    public ProductClient(RestTemplate restTemplate,
                         @Value("${product.service.base-url}") String productServiceBaseUrl) {
        this.restTemplate         = restTemplate;
        this.productServiceBaseUrl = productServiceBaseUrl;
    }

    /**
     * Fetches all products from the Product Service.
     *
     * @return list of products (may be empty)
     */
    public List<Product> getAllProducts() {
        Product[] products = restTemplate
                .getForObject(productServiceBaseUrl + "/api/products", Product[].class);
        return products != null ? Arrays.asList(products) : List.of();
    }

    /**
     * Fetches a single product by its ID.
     *
     * @param id product ID
     * @return an {@link Optional} containing the product, or empty if 404
     */
    public Optional<Product> getProductById(Long id) {
        try {
            Product product = restTemplate
                    .getForObject(productServiceBaseUrl + "/api/products/" + id, Product.class);
            return Optional.ofNullable(product);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    /**
     * Fetches products by category from the Product Service.
     *
     * @param category category name
     * @return list of products in that category
     */
    public List<Product> getProductsByCategory(String category) {
        Product[] products = restTemplate.getForObject(
                productServiceBaseUrl + "/api/products/category/" + category,
                Product[].class);
        return products != null ? Arrays.asList(products) : List.of();
    }
}
