package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.OrderResponse.OrderItemResponse;
import com.ecommerce.order.model.OrderItem;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {
    
    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "productSku", source = "productSku")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "notes", source = "notes")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
