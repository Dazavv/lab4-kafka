package com.hs.lab4.notificationservice.application.service;

import com.hs.lab4.notificationservice.domain.model.EventNotificationMessage;
import com.hs.lab4.notificationservice.infrastructure.entity.EventNotificationEntity;
import com.hs.lab4.notificationservice.infrastructure.persistence.EventNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventNotificationService {
    private final EventNotificationRepository repository;

    @Transactional
    public void handleEventNotification(EventNotificationMessage message) {
        EventNotificationEntity entity = EventNotificationEntity.builder()
                .eventId(message.getEventId())
                .userId(message.getUserId())
                .message(message.getMessage())
                .build();

        repository.save(entity);
    }
}
