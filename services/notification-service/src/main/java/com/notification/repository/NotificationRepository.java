package com.notification.repository;

import com.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    Page<Notification> findByUserId(String userId, Pageable pageable);

    Page<Notification> findByUserIdAndRead(String userId, Boolean read, Pageable pageable);

    Page<Notification> findByUserIdAndType(String userId, String type, Pageable pageable);

    Page<Notification> findByUserIdAndReadAndType(String userId, Boolean read,
                                                   String type, Pageable pageable);

    long countByUserIdAndRead(String userId, Boolean read);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.userId = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") String userId);
}
