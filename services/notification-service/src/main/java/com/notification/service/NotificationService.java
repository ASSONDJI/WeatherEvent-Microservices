package com.notification.service;

import com.notification.dto.generated.*;
import com.notification.exception.ResourceNotFoundException;
import com.notification.model.Notification;
import com.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository repository;

    @Transactional
    public Notification save(String userId, String type, String title,
                              String message, String reservationId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setReservationId(reservationId);
        Notification saved = repository.save(n);
        log.info("Notification saved for user: {} type: {}", userId, type);
        return saved;
    }

    public PagedNotificationResponse getByUser(String userId, Boolean read,
            String type, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<Notification> result;
        if (read != null && type != null) {
            result = repository.findByUserIdAndReadAndType(userId, read, type, pageable);
        } else if (read != null) {
            result = repository.findByUserIdAndRead(userId, read, pageable);
        } else if (type != null) {
            result = repository.findByUserIdAndType(userId, type, pageable);
        } else {
            result = repository.findByUserId(userId, pageable);
        }
        return toPagedResponse(result);
    }

    public NotificationResponse getById(String id) {
        return toResponse(findById(id));
    }

    public UnreadCountResponse getUnreadCount(String userId) {
        long count = repository.countByUserIdAndRead(userId, false);
        UnreadCountResponse response = new UnreadCountResponse();
        response.setUserId(userId);
        response.setUnreadCount(count);
        return response;
    }

    @Transactional
    public NotificationResponse markAsRead(String id) {
        Notification n = findById(id);
        n.setRead(true);
        n.setReadAt(LocalDateTime.now());
        return toResponse(repository.save(n));
    }

    @Transactional
    public BulkOperationResponse markAllAsRead(String userId) {
        int count = repository.markAllAsRead(userId);
        BulkOperationResponse response = new BulkOperationResponse();
        response.setAffectedCount(count);
        response.setMessage(count + " notifications marked as read");
        return response;
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Notification", id);
        }
        repository.deleteById(id);
        log.info("Notification deleted: {}", id);
    }

    private Notification findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            Sort.Direction direction = "desc".equalsIgnoreCase(parts[1]) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
            return PageRequest.of(page, size, Sort.by(direction, parts[0]));
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private PagedNotificationResponse toPagedResponse(Page<Notification> page) {
        PagedNotificationResponse response = new PagedNotificationResponse();
        response.setContent(page.getContent().stream()
            .map(this::toResponse).collect(Collectors.toList()));
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }

    public NotificationResponse toResponse(Notification n) {
        NotificationResponse res = new NotificationResponse();
        res.setId(n.getId());
        res.setUserId(n.getUserId());
        res.setType(n.getType() != null ?
            NotificationType.fromValue(n.getType()) : null);
        res.setTitle(n.getTitle());
        res.setMessage(n.getMessage());
        res.setReservationId(n.getReservationId());
        res.setRead(n.getRead());
        res.setCreatedAt(n.getCreatedAt() != null ?
            n.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
        res.setReadAt(n.getReadAt() != null ?
            n.getReadAt().atOffset(ZoneOffset.UTC) : null);
        return res;
    }
}
