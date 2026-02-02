package com.resourcebridge.events;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminVerificationEvent implements Serializable {

    private String eventId;
    private Instant timestamp;
    private String source;

    private EventType eventType;

    private Long userId;

    private String adminId;

    private AdminActionType action;

    private String remarks;

    private Boolean verified;
}
