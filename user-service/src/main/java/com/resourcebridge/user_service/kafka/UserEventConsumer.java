package com.resourcebridge.user_service.kafka;


import com.resourcebridge.events.UserEvent;
import com.resourcebridge.user_service.entity.UserProfile;
import com.resourcebridge.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {


    private final UserRepository profileRepository;


    @KafkaListener(
            topics = "user-events",
            containerFactory = "userEventFactory"
    )
    public void consume(UserEvent event) {

        log.info("Received UserEvent: {}", event);

        if (event == null || event.getEventType() == null) {

            log.warn("Invalid event received: {}", event);
            return;
        }

        switch (event.getEventType()) {

            case USER_CREATED -> handleUserCreated(event);

            case USER_VERIFIED -> handleUserVerified(event);

            case USER_BLOCKED -> handleUserBlocked(event);
            case USER_ROLE_UPDATED -> handleRoleUpdated(event);
            default ->
                    log.warn("Unhandled event type: {}",
                            event.getEventType());
        }
    }
    private void handleUserCreated(UserEvent event) {

        log.info("Handling USER_CREATED | userId={}",
                event.getUserId());

        if (profileRepository.existsByUserId(event.getUserId())) {

            log.warn("Profile already exists | userId={}",
                    event.getUserId());
            return;
        }

        UserProfile profile = UserProfile.builder()
                .userId(event.getUserId())
                .email(event.getEmail())
                .role(event.getRole())
                .verified(event.getVerified())
                .active(event.getEnabled())
                .build();

        profileRepository.save(profile);

        log.info("User profile created | userId={}",
                event.getUserId());
    }

    private void handleUserVerified(UserEvent event) {

        log.info("Handling USER_VERIFIED | userId={}",
                event.getUserId());

        profileRepository.findByUserId(event.getUserId())
                .ifPresentOrElse(profile -> {

                    profile.setVerified(true);

                    profileRepository.save(profile);

                    log.info("User profile verified | userId={}",
                            event.getUserId());

                }, () -> {

                    log.warn("Profile not found for verification | userId={}",
                            event.getUserId());
                });
    }

    private void handleUserBlocked(UserEvent event) {

        log.info("Handling USER_BLOCKED | userId={}",
                event.getUserId());

        profileRepository.findByUserId(event.getUserId())
                .ifPresent(profile -> {

                    profile.setActive(false);

                    profileRepository.save(profile);

                    log.info("User profile blocked | userId={}",
                            event.getUserId());
                });
    }

    private void handleRoleUpdated(UserEvent event) {

        log.info("Handling USER_ROLE_UPDATED | userId={}",
                event.getUserId());

        profileRepository.findByUserId(event.getUserId())
                .ifPresent(profile -> {

                    profile.setRole(event.getRole());

                    profileRepository.save(profile);

                    log.info("User role updated | userId={} | role={}",
                            event.getUserId(), event.getRole());
                });
    }
}
