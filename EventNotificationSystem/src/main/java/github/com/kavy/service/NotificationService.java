package github.com.kavy.service;

import github.com.kavy.dto.EventRequest;
import github.com.kavy.exception.EventNotFoundException;
import github.com.kavy.model.EventType;
import github.com.kavy.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PreDestroy;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class NotificationService implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Map<EventType, BlockingQueue<NotificationEvent>> queues = new EnumMap<>(EventType.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(EventType.values().length);
    private final Map<String, NotificationEvent> eventStore = new ConcurrentHashMap<>();
    private final AtomicBoolean acceptingEvents = new AtomicBoolean(true);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final FailureSimulator failureSimulator;
    private final ProcessingDelayProvider delayProvider;
    private final CallbackClient callbackClient;

    public NotificationService(FailureSimulator failureSimulator,
                               ProcessingDelayProvider delayProvider,
                               CallbackClient callbackClient) {
        this.failureSimulator = failureSimulator;
        this.delayProvider = delayProvider;
        this.callbackClient = callbackClient;
        for (EventType type : EventType.values()) {
            queues.put(type, new LinkedBlockingQueue<>());
        }
    }

    public NotificationEvent submitEvent(EventRequest request) {
        if (!acceptingEvents.get()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "System is shutting down. No new events are accepted.");
        }
        NotificationEvent event = NotificationEvent.of(
                request.getEventType(),
                request.getPayload(),
                request.getCallbackUrl()
        );
        eventStore.put(event.getEventId(), event);
        queues.get(event.getEventType()).offer(event);
        log.info("Event {} accepted into {} queue", event.getEventId(), event.getEventType());
        return event;
    }

    public NotificationEvent getEvent(String eventId) {
        return Optional.ofNullable(eventStore.get(eventId))
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    public int getQueueDepth(EventType type) {
        return queues.get(type).size();
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            for (EventType type : EventType.values()) {
                executorService.submit(() -> processQueue(type));
                log.info("{} processor started", type);
            }
        }
    }

    private void processQueue(EventType type) {
        BlockingQueue<NotificationEvent> queue = queues.get(type);
        while (running.get() || !queue.isEmpty()) {
            try {
                NotificationEvent event = queue.poll(500, TimeUnit.MILLISECONDS);
                if (event == null) {
                    continue;
                }
                event.markProcessing();
                handleEvent(event);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception exception) {
                log.error("Error while processing {} queue: {}", type, exception.getMessage());
            }
        }
        log.info("{} processor stopped", type);
    }

    private void handleEvent(NotificationEvent event) throws InterruptedException {
        delayProvider.delayFor(event.getEventType());
        if (failureSimulator.shouldFail()) {
            event.markFailed("Simulated processing failure");
            callbackClient.notifyCallback(event);
            log.warn("Event {} failed", event.getEventId());
            return;
        }
        event.markCompleted();
        callbackClient.notifyCallback(event);
        log.info("Event {} completed", event.getEventId());
    }

    @Override
    public void stop() {
        acceptingEvents.set(false);
        running.set(false);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("NotificationService stopped gracefully");
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @PreDestroy
    public void onDestroy() {
        stop();
    }
}


