package github.com.kavy.dto;

import github.com.kavy.model.EventStatus;
import github.com.kavy.model.EventType;

import java.time.Instant;

public record CallbackPayload(
        String eventId,
        EventStatus status,
        EventType eventType,
        Instant processedAt,
        String errorMessage
) {
}


