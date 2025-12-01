package github.com.kavy.exception;

public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String eventId) {
        super("Event with id %s not found".formatted(eventId));
    }
}


