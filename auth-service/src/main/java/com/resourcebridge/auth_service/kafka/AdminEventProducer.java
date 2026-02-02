package com.resourcebridge.auth_service.kafka;


import com.resourcebridge.events.AdminVerificationEvent;
import com.resourcebridge.events.EventTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminEventProducer {

    private final KafkaTemplate<String, AdminVerificationEvent> kafkaTemplate;

    public void sendAdminVerificationEvent(AdminVerificationEvent event) {

        log.info("Publishing AdminVerificationEvent: {}", event);

        kafkaTemplate.send(
                EventTopics.ADMIN_EVENTS,
                event.getUserId().toString(),
                event
        );
    }
}
