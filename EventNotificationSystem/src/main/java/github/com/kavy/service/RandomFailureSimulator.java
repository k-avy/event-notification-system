package github.com.kavy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class RandomFailureSimulator implements FailureSimulator {

    private final double failureRate;

    public RandomFailureSimulator(@Value("${app.failure-rate:0.1}") double failureRate) {
        this.failureRate = failureRate;
    }

    @Override
    public boolean shouldFail() {
        return ThreadLocalRandom.current().nextDouble() < failureRate;
    }
}


