package github.com.kavy.dto;

import github.com.kavy.model.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.util.Map;

public class EventRequest {

    @NotNull(message = "eventType is required")
    private EventType eventType;

    @NotNull(message = "payload is required")
    private Map<String, Object> payload;

    @NotBlank(message = "callbackUrl is required")
    @URL(message = "callbackUrl must be a valid URL")
    private String callbackUrl;

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}


