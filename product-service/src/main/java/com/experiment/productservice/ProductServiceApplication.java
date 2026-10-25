package com.experiment.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Product Service microservice.
 *
 * <p>This service exposes a REST API for managing products and acts as the
 * <b>Pact provider</b> in contract testing between the Order Service (consumer)
 * and this service (producer).
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
