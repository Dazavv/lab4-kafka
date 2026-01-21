package com.hs.lab4.notificationservice.infrastructure.persistence;

import com.hs.lab4.notificationservice.infrastructure.entity.EventNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventNotificationRepository extends JpaRepository<EventNotificationEntity, Long> {
}
