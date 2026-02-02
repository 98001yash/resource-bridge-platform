package com.resourcebridge.events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent {

    private String eventId;
    private String source;
    private EventType eventType;
    private Long userId;
    private String email;
    private String role;
    private Boolean verified;
    private String fullName;
    private Boolean enabled;
}
