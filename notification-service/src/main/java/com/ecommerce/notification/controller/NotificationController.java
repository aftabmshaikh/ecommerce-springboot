package com.ecommerce.notification.controller;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Long>> getPendingNotificationCount(@RequestParam String email) {
        long count = notificationService.getPendingNotificationCount(email);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping
    public ResponseEntity<Void> sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestParam Notification.NotificationType type) {
        
        notificationService.sendEmailNotification(to, subject, content, type);
        return ResponseEntity.accepted().build();
    }
}
