package github.com.kavy.dto;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        String message,
        String path
) {
    public static ErrorResponse of(String message, String path) {
        return new ErrorResponse(Instant.now(), message, path);
    }
}


