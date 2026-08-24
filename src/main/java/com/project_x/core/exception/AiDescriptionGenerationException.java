package com.project_x.core.exception;

public class AiDescriptionGenerationException extends RuntimeException {

    public AiDescriptionGenerationException(String message) {
        super(message);
    }

    public AiDescriptionGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
