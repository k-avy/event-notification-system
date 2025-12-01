package github.com.kavy.service;

import github.com.kavy.dto.CallbackPayload;
import github.com.kavy.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class CallbackClient {

    private static final Logger log = LoggerFactory.getLogger(CallbackClient.class);
    private final RestClient restClient;

    public CallbackClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public void notifyCallback(NotificationEvent event) {
        String callbackUrl = event.getCallbackUrl();
        if (callbackUrl == null) {
            log.warn("Skipping callback for event {} because callbackUrl is null", event.getEventId());
            return;
        }

        CallbackPayload payload = new CallbackPayload(
                event.getEventId(),
                event.getStatus(),
                event.getEventType(),
                event.getProcessedAt(),
                event.getErrorMessage()
        );

        try {
            restClient.post()
                    .uri(callbackUrl)
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Callback sent for event {}", event.getEventId());
        } catch (Exception exception) {
            log.error("Failed to send callback for event {}: {}", event.getEventId(), exception.getMessage());
        }
    }
}


