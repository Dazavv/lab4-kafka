package com.hs.lab4.notificationservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessagingService {
    private static final String topicAddRole = "${topic.user-role-added}";
    private static final String kafkaConsumerGroupId = "${spring.kafka.consumer.group-id}";

    @Transactional
    @KafkaListener(topics = topicAddRole, groupId = kafkaConsumerGroupId, properties = {"spring.json.value.default.type=com.hs.lab4.notificationservice.UserRoleAddedEvent"})
    public UserRoleAddedEvent printEvent(UserRoleAddedEvent userRoleAddedEvent) {
        log.info("User with login: " + userRoleAddedEvent.getLogin() + " has new role: " + userRoleAddedEvent.getRole());
        return userRoleAddedEvent;
    }
}
