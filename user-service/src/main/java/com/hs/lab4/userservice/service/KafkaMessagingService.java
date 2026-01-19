package com.hs.lab4.userservice.service;

import com.hs.lab4.userservice.dto.kafkaDto.UserRoleAddedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@RequiredArgsConstructor
public class KafkaMessagingService {
    @Value("${topic.user-role-added}")
    private String sendClientTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Mono<Void> publishRoleAddedEvent(UserRoleAddedEvent userRoleAddedEvent) {
        return Mono.fromRunnable(() ->
                kafkaTemplate.send(sendClientTopic, userRoleAddedEvent)
        );
    }
}
