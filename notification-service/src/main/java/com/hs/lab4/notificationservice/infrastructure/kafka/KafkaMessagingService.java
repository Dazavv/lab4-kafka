package com.hs.lab4.notificationservice.infrastructure.kafka;

import com.hs.lab4.notificationservice.application.service.EventNotificationService;
import com.hs.lab4.notificationservice.application.service.GroupEventNotificationService;
import com.hs.lab4.notificationservice.application.service.UserRoleNotificationService;
import com.hs.lab4.notificationservice.domain.model.EventNotificationMessage;
import com.hs.lab4.notificationservice.domain.model.GroupEventNotificationMessage;
import com.hs.lab4.notificationservice.domain.model.UserRoleAddedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessagingService {
    private static final String topicAddRole = "${topic.user-role-added}";
    private static final String topicCreateEvent = "${topic.event-notifications}";
    private static final String topicCreateGroupEvent = "${topic.group-event-notifications}";
    private static final String kafkaConsumerGroupId = "${spring.kafka.consumer.group-id}";

    private final UserRoleNotificationService userRoleNotificationService;
    private final EventNotificationService eventNotificationService;
    private final GroupEventNotificationService groupEventNotificationService;

    @KafkaListener(
            topics = topicAddRole,
            groupId = kafkaConsumerGroupId,
            properties = {"spring.json.value.default.type=com.hs.lab4.notificationservice.domain.model.UserRoleAddedEvent"})
    public void consumeUserRoleAdded(UserRoleAddedEvent event) {
        log.info("Received UserRoleAddedEvent for login: {}, role: {}", event.getLogin(), event.getRole());
        userRoleNotificationService.handleUserRoleAdded(event);
    }

    @KafkaListener(
            topics = topicCreateEvent,
            groupId = kafkaConsumerGroupId,
            properties = {"spring.json.value.default.type=com.hs.lab4.notificationservice.domain.model.EventNotificationMessage"}
    )
    public void consumeEventNotification(EventNotificationMessage message) {
        log.info("Received EventNotificationMessage for eventId: {}", message.getEventId());
        eventNotificationService.handleEventNotification(message);
    }

    @KafkaListener(
            topics = topicCreateGroupEvent,
            groupId = kafkaConsumerGroupId,
            properties = {"spring.json.value.default.type=com.hs.lab4.notificationservice.domain.model.GroupEventNotificationMessage"}
    )
    public void consumeGroupEventNotification(GroupEventNotificationMessage message) {
        log.info("Received GroupEventNotificationMessage for eventId: {}", message.getEventId());
        groupEventNotificationService.handleEventNotification(message);
    }

}
