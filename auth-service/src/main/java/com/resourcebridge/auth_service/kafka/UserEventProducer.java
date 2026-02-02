package com.resourcebridge.auth_service.kafka;


import com.resourcebridge.events.EventTopics;
import com.resourcebridge.events.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void sendUserEvent(UserEvent event) {

        log.info("Publishing UserEvent: {}", event);

        kafkaTemplate.send(
                EventTopics.USER_EVENTS,
                event.getUserId().toString(),
                event
        );
    }
}
