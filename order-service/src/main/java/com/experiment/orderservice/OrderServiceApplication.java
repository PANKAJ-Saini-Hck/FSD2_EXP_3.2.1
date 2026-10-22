package com.experiment.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Order Service microservice.
 *
 * <p>This service acts as the <b>Pact consumer</b>: it defines what it
 * expects from the Product Service API. Running its contract tests generates
 * the Pact JSON files that the Product Service (provider) must verify.
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
