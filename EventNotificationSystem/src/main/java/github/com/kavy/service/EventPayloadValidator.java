package github.com.kavy.service;

import github.com.kavy.dto.EventRequest;
import github.com.kavy.exception.PayloadValidationException;
import github.com.kavy.model.EventType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class EventPayloadValidator {

    public void validate(EventRequest request) {
        Map<String, Object> payload = request.getPayload();
        if (payload == null || payload.isEmpty()) {
            throw new PayloadValidationException("payload cannot be empty");
        }

        EventType eventType = request.getEventType();
        switch (eventType) {
            case EMAIL -> validateEmailPayload(payload);
            case SMS -> validateSmsPayload(payload);
            case PUSH -> validatePushPayload(payload);
            default -> throw new PayloadValidationException("Unsupported eventType: " + eventType);
        }
    }

    private void validateEmailPayload(Map<String, Object> payload) {
        String recipient = readString(payload, "recipient");
        String message = readString(payload, "message");
        if (!recipient.contains("@")) {
            throw new PayloadValidationException("recipient must be a valid email address");
        }
        requireMessage(message);
    }

    private void validateSmsPayload(Map<String, Object> payload) {
        String phone = readString(payload, "phoneNumber");
        String message = readString(payload, "message");
        if (phone.length() < 8) {
            throw new PayloadValidationException("phoneNumber must be at least 8 characters");
        }
        requireMessage(message);
    }

    private void validatePushPayload(Map<String, Object> payload) {
        readString(payload, "deviceId");
        requireMessage(readString(payload, "message"));
    }

    private void requireMessage(String message) {
        if (!StringUtils.hasText(message)) {
            throw new PayloadValidationException("message is required");
        }
    }

    private String readString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (!(value instanceof String str) || !StringUtils.hasText(str)) {
            throw new PayloadValidationException("%s is required".formatted(key));
        }
        return str;
    }
}


