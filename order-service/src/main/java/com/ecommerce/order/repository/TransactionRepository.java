package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByOrder(Order order);
    List<Transaction> findByCustomerId(UUID customerId);
    boolean existsByTransactionId(String transactionId);
    boolean existsByGatewayTransactionId(String gatewayTransactionId);
}
