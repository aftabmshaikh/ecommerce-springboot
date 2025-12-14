package com.ecommerce.order.client;

import com.ecommerce.order.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(
    name = "customer-service",
    configuration = FeignClientConfig.class
)
public interface CustomerServiceClient {

    @GetMapping("/api/customers/{customerId}/exists")
    boolean customerExists(@PathVariable UUID customerId);

    @GetMapping("/api/customers/{customerId}/has-purchased-product/{productId}")
    boolean hasPurchasedProduct(
            @PathVariable UUID customerId,
            @PathVariable UUID productId,
            @RequestHeader("Authorization") String token);
}
