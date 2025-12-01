package github.com.kavy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.com.kavy.dto.EventRequest;
import github.com.kavy.model.EventStatus;
import github.com.kavy.model.EventType;
import github.com.kavy.model.NotificationEvent;
import github.com.kavy.service.EventPayloadValidator;
import github.com.kavy.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Objects;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {EventController.class, RestExceptionHandler.class})
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private EventPayloadValidator payloadValidator;

    @Test
    void enqueueEvent_ShouldReturnAccepted() throws Exception {
        EventRequest request = new EventRequest();
        request.setEventType(EventType.EMAIL);
        request.setPayload(Map.of("recipient", "user@example.com", "message", "hi"));
        request.setCallbackUrl("http://localhost/callback");

        NotificationEvent event = NotificationEvent.of(EventType.EMAIL, request.getPayload(), request.getCallbackUrl());
        when(notificationService.submitEvent(ArgumentMatchers.any())).thenReturn(event);
        doNothing().when(payloadValidator).validate(ArgumentMatchers.any());

        mockMvc.perform(post("/api/events")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").value(event.getEventId()));
    }

    @Test
    void getStatus_ShouldReturnEvent() throws Exception {
        NotificationEvent event = NotificationEvent.of(EventType.SMS, Map.of("phoneNumber", "+12345678", "message", "hello"), "http://callback");
        event.markCompleted();
        when(notificationService.getEvent(event.getEventId())).thenReturn(event);

        mockMvc.perform(get("/api/events/{id}", event.getEventId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(event.getEventId()))
                .andExpect(jsonPath("$.status").value(EventStatus.COMPLETED.name()));
    }
}


