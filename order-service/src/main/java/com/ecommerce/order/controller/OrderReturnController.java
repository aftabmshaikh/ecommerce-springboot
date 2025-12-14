package com.ecommerce.order.controller;

import com.ecommerce.order.dto.returns.ReturnRequest;
import com.ecommerce.order.dto.returns.ReturnResponse;
import com.ecommerce.order.service.OrderReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Return API", description = "APIs for order returns")
public class OrderReturnController {

    private final OrderReturnService returnService;

    @PostMapping("/{orderId}/return")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initiate an order return")
    public ReturnResponse initiateReturn(
            @PathVariable UUID orderId,
            @Valid @RequestBody ReturnRequest request) {
        return returnService.initiateReturn(orderId, request);
    }

    @GetMapping("/returns")
    @Operation(summary = "Get return history for the current user")
    public Page<ReturnResponse> getReturnHistory(Pageable pageable) {
        return returnService.getReturnHistory(pageable);
    }

    @GetMapping("/returns/{returnId}")
    @Operation(summary = "Get return details")
    public ReturnResponse getReturnDetails(@PathVariable UUID returnId) {
        return returnService.getReturnDetails(returnId);
    }
}
