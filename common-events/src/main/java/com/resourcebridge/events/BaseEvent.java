package com.resourcebridge.events;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BaseEvent implements Serializable {

    private String eventId = UUID.randomUUID().toString();
    private Instant timestamp = Instant.now();
    private String source;
}
