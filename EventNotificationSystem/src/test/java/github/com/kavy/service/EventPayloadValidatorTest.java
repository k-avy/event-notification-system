package github.com.kavy.service;

import github.com.kavy.dto.EventRequest;
import github.com.kavy.exception.PayloadValidationException;
import github.com.kavy.model.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class EventPayloadValidatorTest {

    private EventPayloadValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EventPayloadValidator();
    }

    @Test
    void validate_ShouldPassForValidEmailPayload() {
        EventRequest request = buildRequest(EventType.EMAIL,
                Map.of("recipient", "user@example.com", "message", "hello"));

        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    void validate_ShouldFailForInvalidEmailPayload() {
        EventRequest request = buildRequest(EventType.EMAIL,
                Map.of("recipient", "user", "message", ""));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(PayloadValidationException.class);
    }

    @Test
    void validate_ShouldFailWhenMessageMissing() {
        EventRequest request = buildRequest(EventType.SMS,
                Map.of("phoneNumber", "+123456789"));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(PayloadValidationException.class);
    }

    private EventRequest buildRequest(EventType type, Map<String, Object> payload) {
        EventRequest request = new EventRequest();
        request.setEventType(type);
        request.setPayload(payload);
        request.setCallbackUrl("http://localhost/callback");
        return request;
    }
}


