package github.com.kavy.service;

import github.com.kavy.dto.EventRequest;
import github.com.kavy.model.EventStatus;
import github.com.kavy.model.EventType;
import github.com.kavy.model.NotificationEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private FailureSimulator failureSimulator;
    private ProcessingDelayProvider delayProvider;
    private CallbackClient callbackClient;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        failureSimulator = mock(FailureSimulator.class);
        delayProvider = type -> { /* no-op for tests */ };
        callbackClient = mock(CallbackClient.class);
        notificationService = new NotificationService(failureSimulator, delayProvider, callbackClient);
        notificationService.start();
    }

    @AfterEach
    void tearDown() {
        notificationService.stop();
    }

    @Test
    void submitEvent_ShouldProcessSuccessfully() {
        when(failureSimulator.shouldFail()).thenReturn(false);
        EventRequest request = request(EventType.EMAIL);

        NotificationEvent event = notificationService.submitEvent(request);

        Awaitility.await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(event.getStatus()).isEqualTo(EventStatus.COMPLETED));
        verify(callbackClient, times(1)).notifyCallback(event);
    }

    @Test
    void submitEvent_ShouldMarkFailedWhenSimulatorTriggers() {
        when(failureSimulator.shouldFail()).thenReturn(true);
        EventRequest request = request(EventType.SMS);

        NotificationEvent event = notificationService.submitEvent(request);

        Awaitility.await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(event.getStatus()).isEqualTo(EventStatus.FAILED));
        verify(callbackClient, times(1)).notifyCallback(event);
    }

    @Test
    void stop_ShouldRejectNewEvents() {
        notificationService.stop();

        assertThatThrownBy(() -> notificationService.submitEvent(request(EventType.PUSH)))
                .hasMessageContaining("System is shutting down");
    }

    private EventRequest request(EventType type) {
        EventRequest request = new EventRequest();
        request.setEventType(type);
        String key = switch (type) {
            case EMAIL -> "recipient";
            case SMS -> "phoneNumber";
            case PUSH -> "deviceId";
        };
        String value = switch (type) {
            case EMAIL -> "user@example.com";
            case SMS -> "+1234567890";
            case PUSH -> "device-123";
        };
        request.setPayload(Map.of(
                key, value,
                "message", "hello"
        ));
        request.setCallbackUrl("http://localhost/callback");
        return request;
    }
}


