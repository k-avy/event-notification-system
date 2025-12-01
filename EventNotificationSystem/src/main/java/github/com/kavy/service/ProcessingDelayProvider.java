package github.com.kavy.service;

import github.com.kavy.model.EventType;

public interface ProcessingDelayProvider {
    void delayFor(EventType type) throws InterruptedException;
}


