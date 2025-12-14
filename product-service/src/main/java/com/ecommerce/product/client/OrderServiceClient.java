package com.ecommerce.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "order-service", url = "${order.service.url:}")
public interface OrderServiceClient {

    @GetMapping("/api/orders/has-purchased")
    boolean hasPurchasedProduct(
        @RequestParam("customerId") UUID customerId,
        @RequestParam("productId") UUID productId,
        @RequestParam("orderId") UUID orderId
    );
}
