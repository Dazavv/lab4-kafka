package com.hs.lab4.notificationservice.application.service;

import com.hs.lab4.notificationservice.domain.model.EventNotificationMessage;
import com.hs.lab4.notificationservice.domain.model.GroupEventNotificationMessage;
import com.hs.lab4.notificationservice.infrastructure.entity.EventNotificationEntity;
import com.hs.lab4.notificationservice.infrastructure.entity.GroupEventNotificationEntity;
import com.hs.lab4.notificationservice.infrastructure.persistence.EventNotificationRepository;
import com.hs.lab4.notificationservice.infrastructure.persistence.GroupEventNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupEventNotificationService {
    private final GroupEventNotificationRepository repository;

    @Transactional
    public void handleEventNotification(GroupEventNotificationMessage message) {
        GroupEventNotificationEntity entity = GroupEventNotificationEntity.builder()
                .groupEventId(message.getEventId())
                .userId(message.getUserId())
                .message(message.getMessage())
                .build();

        repository.save(entity);
    }
}
