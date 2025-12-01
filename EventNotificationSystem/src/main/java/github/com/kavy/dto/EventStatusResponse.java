package github.com.kavy.dto;

import github.com.kavy.model.EventStatus;
import github.com.kavy.model.EventType;

import java.time.Instant;

public record EventStatusResponse(
        String eventId,
        EventType eventType,
        EventStatus status,
        Instant createdAt,
        Instant processedAt,
        String errorMessage
) {
}


