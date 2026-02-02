package com.resourcebridge.user_service.kafka;

import com.resourcebridge.events.AdminVerificationEvent;
import com.resourcebridge.user_service.entity.UserProfile;
import com.resourcebridge.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEventConsumer {

    private final UserRepository repository;

    @KafkaListener(
            topics = "user-verified-events",
            containerFactory = "userVerifiedFactory"
    )
    public void consume(AdminVerificationEvent event) {

        log.info("Admin verification event received | userId={}",
                event.getUserId());

        UserProfile profile =
                repository.findByUserId(event.getUserId())
                        .orElse(null);

        if (profile == null) {
            log.warn("Profile not found for userId={}", event.getUserId());
            return;
        }

        profile.setVerified(true);

        repository.save(profile);

        log.info("User verified | userId={} | verifiedBy={}",
                event.getUserId(),
                event.getVerified());
    }
}