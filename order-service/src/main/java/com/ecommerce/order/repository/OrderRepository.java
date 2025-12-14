package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByCustomerId(UUID customerId);
    
    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);
    
    List<Order> findByStatus(OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.status = :status")
    Page<Order> findByCustomerIdAndStatus(@Param("customerId") UUID customerId, 
                                        @Param("status") OrderStatus status,
                                        Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.customerEmail = :email ORDER BY o.createdAt DESC")
    Page<Order> findByCustomerEmail(@Param("email") String email, Pageable pageable);
    
    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.productId = :productId")
    List<Order> findOrdersContainingProduct(@Param("productId") UUID productId);
    
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.customerId = :customerId AND o.id = :orderId")
    boolean existsByIdAndCustomerId(@Param("orderId") UUID orderId, 
                                  @Param("customerId") UUID customerId);
}
