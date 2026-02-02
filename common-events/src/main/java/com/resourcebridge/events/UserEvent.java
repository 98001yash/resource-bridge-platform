package com.resourcebridge.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserEvent extends BaseEvent {

    private EventType eventType;
    private Long userId;
    private String email;
    private String role;
    private Boolean verified;
    private Boolean enabled;

    @Builder
    public UserEvent(
            String source,
            EventType eventType,
            Long userId,
            String email,
            String role,
            Boolean verified,
            Boolean enabled
    ) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.verified = verified;
        this.enabled = enabled;
    }
}