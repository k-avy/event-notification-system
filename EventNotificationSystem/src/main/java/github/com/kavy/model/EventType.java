package github.com.kavy.model;

public enum EventType {
    EMAIL(5000),
    SMS(3000),
    PUSH(2000);

    private final long processingDelayMillis;

    EventType(long processingDelayMillis) {
        this.processingDelayMillis = processingDelayMillis;
    }

    public long processingDelayMillis() {
        return processingDelayMillis;
    }
}


