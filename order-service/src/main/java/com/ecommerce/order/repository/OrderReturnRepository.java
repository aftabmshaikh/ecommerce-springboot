package com.ecommerce.order.repository;

import com.ecommerce.order.model.OrderReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturn, UUID> {
    // Custom query methods can be added here
}
