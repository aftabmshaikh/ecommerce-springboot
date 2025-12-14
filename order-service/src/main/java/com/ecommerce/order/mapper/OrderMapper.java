package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.orderitem.OrderItemRequest;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.OrderStatusHistory;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {OrderItemMapper.class},
        imports = {LocalDateTime.class, ChronoUnit.class})
@Component
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", expression = "java(com.ecommerce.order.model.OrderStatus.PENDING)")
    @Mapping(target = "statusHistory", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toEntity(OrderRequest request);
    
    default OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerId(order.getCustomerId());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setCustomerPhone(order.getCustomerPhone());
        response.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        response.setShippingAddress(order.getShippingAddress());
        response.setBillingAddress(order.getBillingAddress());
        response.setSubtotal(order.getSubtotal());
        response.setTax(order.getTax());
        response.setShippingFee(order.getShippingFee());
        response.setTotal(order.getTotal());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        
        if (order.getItems() != null) {
            response.setItems(order.getItems().stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList()));
        }
        
        if (order.getStatusHistory() != null) {
            response.setStatusHistory(mapStatusHistory(order.getStatusHistory()));
        }
        
        return response;
    }
    
    default List<OrderResponse.OrderStatusHistoryResponse> mapStatusHistory(List<OrderStatusHistory> statusHistory) {
        if (statusHistory == null) {
            return null;
        }
        return statusHistory.stream()
                .map(this::toStatusHistoryResponse)
                .collect(Collectors.toList());
    }
    
    default OrderResponse.OrderStatusHistoryResponse toStatusHistoryResponse(OrderStatusHistory history) {
        if (history == null) {
            return null;
        }
        
        return OrderResponse.OrderStatusHistoryResponse.builder()
                .status(history.getStatus() != null ? history.getStatus().name() : null)
                .message(history.getMessage())
                .statusDate(history.getStatusDate())
                .build();
    }

    @AfterMapping
    default void afterToEntity(@MappingTarget Order order, OrderRequest request) {
        if (order != null && request != null && request.getItems() != null) {
            List<OrderItem> items = request.getItems().stream()
                    .map(itemRequest -> {
                        OrderItem item = toOrderItem(itemRequest);
                        if (item != null) {
                            item.setOrder(order);
                        }
                        return item;
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            order.setItems(items);
            order.calculateTotals();
        }
    }

    default String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    default OrderItem toOrderItem(OrderItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }
        return OrderItem.builder()
                .productId(UUID.fromString(itemRequest.getProductId()))
                .productName(itemRequest.getProductName())
                .productSku(itemRequest.getSku())
                .quantity(itemRequest.getQuantity())
                .unitPrice(itemRequest.getUnitPrice())
                .build();
    }
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "productSku", source = "productSku")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "totalPrice", expression = "java(item.getUnitPrice().multiply(new java.math.BigDecimal(item.getQuantity())))")
    @Mapping(target = "notes", source = "notes")
    OrderResponse.OrderItemResponse toOrderItemResponse(OrderItem item);
    
    default org.springframework.data.domain.Page<OrderResponse> toResponsePage(org.springframework.data.domain.Page<Order> orderPage) {
        if (orderPage == null) {
            return org.springframework.data.domain.Page.empty();
        }
        return orderPage.map(this::toResponse);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateOrderFromRequest(OrderRequest request, @MappingTarget Order order);
}
