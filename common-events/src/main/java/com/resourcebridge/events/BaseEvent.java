package com.resourcebridge.events;


import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
public abstract class BaseEvent implements Serializable {

    private String eventId;
    private Instant timestamp;

    private String source;


    protected BaseEvent(String source){
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant .now();
        this.source = source;
    }
}
