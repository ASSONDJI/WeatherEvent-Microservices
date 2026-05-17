package com.notification.controller;

import com.notification.api.NotificationsApi;
import com.notification.dto.generated.*;
import com.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationsApi {

    private final NotificationService service;

    @Override
    public ResponseEntity<PagedNotificationResponse> getNotificationsByUser(
            String userId, Boolean read, NotificationType type,
            Integer page, Integer size, String sort) {
        return ResponseEntity.ok(service.getByUser(
            userId, read,
            type != null ? type.getValue() : null,
            page != null ? page : 0,
            size != null ? size : 10,
            sort != null ? sort : "createdAt,desc"));
    }

    @Override
    public ResponseEntity<UnreadCountResponse> getUnreadCount(String userId) {
        return ResponseEntity.ok(service.getUnreadCount(userId));
    }

    @Override
    public ResponseEntity<BulkOperationResponse> markAllAsRead(String userId) {
        return ResponseEntity.ok(service.markAllAsRead(userId));
    }

    @Override
    public ResponseEntity<NotificationResponse> getNotificationById(String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Override
    public ResponseEntity<NotificationResponse> markAsRead(String id) {
        return ResponseEntity.ok(service.markAsRead(id));
    }

    @Override
    public ResponseEntity<Void> deleteNotification(String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
