package github.com.kavy.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class NotificationEvent {
    private final String eventId;
    private final EventType eventType;
    private final Map<String, Object> payload;
    private final String callbackUrl;
    private final Instant createdAt;
    private volatile EventStatus status;
    private volatile Instant processedAt;
    private volatile String errorMessage;

    private NotificationEvent(EventType eventType,
                              Map<String, Object> payload,
                              String callbackUrl) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.payload = payload;
        this.callbackUrl = callbackUrl;
        this.createdAt = Instant.now();
        this.status = EventStatus.ACCEPTED;
    }

    public static NotificationEvent of(EventType type,
                                       Map<String, Object> payload,
                                       String callbackUrl) {
        return new NotificationEvent(type, payload, callbackUrl);
    }

    public String getEventId() {
        return eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public EventStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void markProcessing() {
        this.status = EventStatus.PROCESSING;
    }

    public void markCompleted() {
        this.status = EventStatus.COMPLETED;
        this.processedAt = Instant.now();
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = EventStatus.FAILED;
        this.processedAt = Instant.now();
        this.errorMessage = errorMessage;
    }
}


