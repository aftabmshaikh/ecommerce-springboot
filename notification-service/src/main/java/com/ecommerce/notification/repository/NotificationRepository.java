package com.ecommerce.notification.repository;

import com.ecommerce.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByRecipientEmailAndSentFalse(String email);
    List<Notification> findBySentFalse();
    long countByRecipientEmailAndSentFalse(String email);
}
