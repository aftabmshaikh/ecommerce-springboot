package com.ecommerce.notification.service;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    public void sendEmailNotification(String to, String subject, String content, Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .recipientEmail(to)
                .subject(subject)
                .content(content)
                .type(type)
                .sent(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        notification = notificationRepository.save(notification);
        log.info("Notification saved with ID: {}", notification.getId());
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void processPendingNotifications() {
        List<Notification> pendingNotifications = notificationRepository.findBySentFalse();
        log.info("Processing {} pending notifications", pendingNotifications.size());
        
        for (Notification notification : pendingNotifications) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(notification.getRecipientEmail());
                message.setSubject(notification.getSubject());
                message.setText(notification.getContent());
                
                mailSender.send(message);
                
                notification.setSent(true);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
                
                log.info("Notification sent to: {}", notification.getRecipientEmail());
            } catch (Exception e) {
                log.error("Failed to send notification: {}", notification.getId(), e);
            }
        }
    }
    
    public long getPendingNotificationCount(String email) {
        return notificationRepository.countByRecipientEmailAndSentFalse(email);
    }
}
