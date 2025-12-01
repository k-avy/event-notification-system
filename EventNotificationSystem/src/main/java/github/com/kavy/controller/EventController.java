package github.com.kavy.controller;

import github.com.kavy.dto.EventRequest;
import github.com.kavy.dto.EventResponse;
import github.com.kavy.dto.EventStatusResponse;
import github.com.kavy.model.NotificationEvent;
import github.com.kavy.service.EventPayloadValidator;
import github.com.kavy.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final NotificationService notificationService;
    private final EventPayloadValidator payloadValidator;

    public EventController(NotificationService notificationService,
                           EventPayloadValidator payloadValidator) {
        this.notificationService = notificationService;
        this.payloadValidator = payloadValidator;
    }

    @PostMapping
    public ResponseEntity<EventResponse> enqueueEvent(@Valid @RequestBody EventRequest request) {
        payloadValidator.validate(request);
        NotificationEvent event = notificationService.submitEvent(request);
        return ResponseEntity.accepted()
                .body(new EventResponse(event.getEventId(), "Event accepted for processing."));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventStatusResponse> getStatus(@PathVariable String eventId) {
        NotificationEvent event = notificationService.getEvent(eventId);
        EventStatusResponse response = new EventStatusResponse(
                event.getEventId(),
                event.getEventType(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getProcessedAt(),
                event.getErrorMessage()
        );
        return ResponseEntity.ok(response);
    }
}


