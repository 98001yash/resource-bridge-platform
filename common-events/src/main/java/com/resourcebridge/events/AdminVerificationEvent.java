package com.resourcebridge.events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminVerificationEvent extends BaseEvent {

    private EventType eventType;

    private Long userId;

    private String adminId;

    private AdminActionType action;

    private String remarks;

    private Boolean verified;

    @Builder
    public AdminVerificationEvent(
            String source,
            EventType eventType,
            Long userId,
            String adminId,
            AdminActionType action,
            String remarks,
            Boolean verified
    ) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.adminId = adminId;
        this.action = action;
        this.remarks = remarks;
        this.verified = verified;
    }
}