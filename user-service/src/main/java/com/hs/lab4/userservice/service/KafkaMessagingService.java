package com.hs.lab4.userservice.service;

import com.hs.lab4.userservice.dto.kafkaDto.UserRoleAddedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@RequiredArgsConstructor
@Log4j2
public class KafkaMessagingService {
    @Value("${topic.user-role-added}")
    private String sendClientTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public Mono<Void> publishRoleAddedEvent(UserRoleAddedEvent userRoleAddedEvent) {
        log.info("send msg to kafka");
        return Mono.fromRunnable(() ->
                kafkaTemplate.send(sendClientTopic, userRoleAddedEvent)
        );
    }
}
