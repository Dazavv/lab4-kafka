package com.hs.lab4.eventservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventNotificationProducer {

    private final KafkaTemplate<String, EventNotificationMessage> kafkaTemplate;

    private static final String TOPIC = "event-notifications-topic";

    public Mono<Void> sendNotification(EventNotificationMessage message) {
        return Mono.fromRunnable(() -> {
            kafkaTemplate.send(TOPIC, message);
            log.info("Sent notification for event: {}", message.getEventId());
        });
    }
}
