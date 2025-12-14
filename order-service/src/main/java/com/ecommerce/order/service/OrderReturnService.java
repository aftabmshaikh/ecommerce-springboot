package com.ecommerce.order.service;

import com.ecommerce.order.dto.returns.ReturnRequest;
import com.ecommerce.order.dto.returns.ReturnResponse;
import com.ecommerce.order.exception.InvalidReturnRequestException;
import com.ecommerce.order.exception.OrderItemNotFoundException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.*;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.OrderReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderReturnService {

    private final OrderReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final NotificationService notificationService;

    @Transactional
    public ReturnResponse initiateReturn(UUID orderId, ReturnRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        // Validate return request
        validateReturnRequest(order, request);

        // Create return record
        OrderReturn orderReturn = new OrderReturn();
        orderReturn.setReturnNumber(generateReturnNumber());
        orderReturn.setOrderId(orderId);
        orderReturn.setCustomerId(order.getCustomerId());
        orderReturn.setStatus(ReturnStatus.REQUESTED);
        orderReturn.setReturnReason(request.getReason());
        orderReturn.setComments(request.getComments());
        orderReturn.setRequestedDate(LocalDateTime.now());

        // Process return items
        List<OrderReturnItem> returnItems = request.getItems().stream()
                .map(itemRequest -> {
                    OrderItem orderItem = orderItemRepository.findById(itemRequest.getOrderItemId())
                            .orElseThrow(() -> new OrderItemNotFoundException("Order item not found with ID: " + itemRequest.getOrderItemId()));

                    OrderReturnItem returnItem = new OrderReturnItem();
                    returnItem.setOrderItemId(itemRequest.getOrderItemId());
                    returnItem.setProductId(orderItem.getProductId());
                    returnItem.setProductName(orderItem.getProductName());
                    returnItem.setProductSku(orderItem.getProductSku());
                    returnItem.setQuantity(itemRequest.getQuantity());
                    returnItem.setUnitPrice(orderItem.getUnitPrice());
                    returnItem.setRefundAmount(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
                    returnItem.setReason(itemRequest.getReason());
                    returnItem.setStatus(ReturnStatus.REQUESTED);
                    returnItem.setOrderReturn(orderReturn);
                    
                    return returnItem;
                })
                .collect(Collectors.toList());

        orderReturn.setItems(returnItems);
        
        // Calculate total refund amount
        BigDecimal totalRefund = returnItems.stream()
                .map(OrderReturnItem::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        orderReturn.setRefundAmount(totalRefund);
        orderReturn.setRefundMethod(order.getPaymentMethod());

        // Save return
        OrderReturn savedReturn = returnRepository.save(orderReturn);

        // Update order status if needed
        if (order.getStatus() != OrderStatus.RETURN_REQUESTED) {
            order.setStatus(OrderStatus.RETURN_REQUESTED);
            orderRepository.save(order);
        }

        // Send notification
        notificationService.sendReturnRequestReceivedNotification(savedReturn);

        return mapToReturnResponse(savedReturn);
    }

    public Page<ReturnResponse> getReturnHistory(Pageable pageable) {
        // In a real app, you would filter by the current customer ID
        return returnRepository.findAll(pageable)
                .map(this::mapToReturnResponse);
    }

    public ReturnResponse getReturnDetails(UUID returnId) {
        return returnRepository.findById(returnId)
                .map(this::mapToReturnResponse)
                .orElseThrow(() -> new OrderNotFoundException("Return not found with ID: " + returnId));
    }

    private void validateReturnRequest(Order order, ReturnRequest request) {
        // Check if order is eligible for return
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidReturnRequestException("Cannot return a cancelled order");
        }

        if (order.getStatus() == OrderStatus.RETURNED) {
            throw new InvalidReturnRequestException("This order has already been returned");
        }

        // Check if return window is still open (e.g., within 30 days of delivery)
        if (order.getDeliveredAt() != null && 
            order.getDeliveredAt().plusDays(30).isBefore(LocalDateTime.now())) {
            throw new InvalidReturnRequestException("Return window has expired (30 days from delivery)");
        }

        // Validate return items
        request.getItems().forEach(item -> {
            // Check if item exists in the order
            boolean itemExists = order.getItems().stream()
                    .anyMatch(oi -> oi.getId().equals(item.getOrderItemId()));
            
            if (!itemExists) {
                throw new InvalidReturnRequestException("Invalid order item ID: " + item.getOrderItemId());
            }

            // Check if quantity is valid
            if (item.getQuantity() <= 0) {
                throw new InvalidReturnRequestException("Return quantity must be greater than 0");
            }

            // Check if item is already returned
            boolean alreadyReturned = order.getItems().stream()
                    .filter(oi -> oi.getId().equals(item.getOrderItemId()))
                    .findFirst()
                    .map(OrderItem::getReturnedQuantity)
                    .map(returnedQty -> returnedQty + item.getQuantity() > orderItemRepository
                            .findById(item.getOrderItemId())
                            .orElseThrow()
                            .getQuantity())
                    .orElse(false);

            if (alreadyReturned) {
                throw new InvalidReturnRequestException("Return quantity exceeds the original order quantity");
            }
        });
    }

    private String generateReturnNumber() {
        return "RTN-" + System.currentTimeMillis();
    }

    private ReturnResponse mapToReturnResponse(OrderReturn orderReturn) {
        return ReturnResponse.builder()
                .id(orderReturn.getId())
                .returnNumber(orderReturn.getReturnNumber())
                .orderId(orderReturn.getOrderId())
                .orderNumber(orderReturn.getOrder().getOrderNumber())
                .status(orderReturn.getStatus().name())
                .returnReason(orderReturn.getReturnReason())
                .comments(orderReturn.getComments())
                .refundAmount(orderReturn.getRefundAmount())
                .refundMethod(orderReturn.getRefundMethod())
                .requestedDate(orderReturn.getRequestedDate())
                .processedDate(orderReturn.getProcessedDate())
                .items(orderReturn.getItems().stream()
                        .map(this::mapToReturnItemResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private ReturnResponse.ReturnItemResponse mapToReturnItemResponse(OrderReturnItem item) {
        return ReturnResponse.ReturnItemResponse.builder()
                .id(item.getId())
                .orderItemId(item.getOrderItemId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .refundAmount(item.getRefundAmount())
                .reason(item.getReason())
                .status(item.getStatus().name())
                .build();
    }
}
