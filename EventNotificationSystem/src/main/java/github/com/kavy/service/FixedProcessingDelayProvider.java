package github.com.kavy.service;

import github.com.kavy.model.EventType;
import org.springframework.stereotype.Component;

@Component
public class FixedProcessingDelayProvider implements ProcessingDelayProvider {

    @Override
    public void delayFor(EventType type) throws InterruptedException {
        Thread.sleep(type.processingDelayMillis());
    }
}


