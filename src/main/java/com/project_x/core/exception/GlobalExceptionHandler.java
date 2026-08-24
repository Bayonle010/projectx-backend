package com.project_x.core.exception;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Extracts fields and indexes from Jackson paths such as:
     *
     * SaveListingRequest["images"]
     *     -> ArrayList[2]
     *     -> ImageRequest["format"]
     *
     * Result:
     * images[2].format
     */
    private static final Pattern JACKSON_PATH_PATTERN =
            Pattern.compile("\\[\"([^\"]+)\"\\]|\\[(\\d+)]");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        FieldError fieldError = ex.getBindingResult()
                .getFieldError();

        String fieldName = fieldError != null
                ? fieldError.getField()
                : "request";

        String errorMessage = fieldError != null
                ? fieldError.getDefaultMessage()
                : "Request validation failed";

        log.warn(
                "Request validation failed for field '{}': {}",
                fieldName,
                errorMessage
        );

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                "Invalid value for field '" + fieldName + "'",
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(
            AccessDeniedException ex
    ) {
        log.warn("Access denied: {}", ex.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                "Access denied",
                null
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequestException(
            BadRequestException ex
    ) {
        log.warn("Bad request: {}", ex.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "Bad request",
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(
            IllegalArgumentException ex
    ) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "Invalid argument",
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<ApiResponse> handleInvalidCredentialsException(
            InvalidCredentialException ex
    ) {
        log.warn("Invalid credential: {}", ex.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                "Invalid credentials",
                null
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    @ExceptionHandler(NetworkConnectivityException.class)
    public ResponseEntity<ApiResponse> handleNetworkConnectivityException(
            NetworkConnectivityException ex
    ) {
        log.error(
                "Network connectivity issue: {}",
                ex.getMessage(),
                ex
        );

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                "Unable to connect to an external service",
                null
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponse);
    }

    @ExceptionHandler(AiDescriptionGenerationException.class)
    public ResponseEntity<ApiResponse> handleAiDescriptionGenerationException(
            AiDescriptionGenerationException ex
    ) {
        log.error("AI description generation failed: {}", ex.getMessage(), ex);

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Description generation is temporarily unavailable",
                "Please try again shortly",
                null
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(
            ResourceNotFoundException ex
    ) {
        log.warn("Resource not found: {}", ex.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                "Resource not found",
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflictException(
            ConflictException ex
    ) {
        log.warn("Conflict: {}", ex.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                "The request conflicts with an existing resource",
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex
    ) {
        log.warn("Maximum upload size exceeded: {}", ex.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.BAD_REQUEST.value(),
                "Uploaded file exceeds the maximum allowed size",
                "Maximum upload size exceeded",
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleInvalidJson(
            HttpMessageNotReadableException ex
    ) {
        /*
         * Keep the complete technical exception in the logs.
         * Do not return ex.getMessage() directly to the frontend.
         */
        log.warn(
                "Request body could not be deserialized: {}",
                ex.getMessage(),
                ex
        );

        Throwable mostSpecificCause = ex.getMostSpecificCause();

        String readableMessage =
                buildReadableJsonError(mostSpecificCause);

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.BAD_REQUEST.value(),
                readableMessage,
                "Invalid request body",
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex
    ) {
        String method = ex.getMethod();

        String supportedMethods =
                ex.getSupportedHttpMethods() == null
                        ? "the correct HTTP method"
                        : ex.getSupportedHttpMethods().toString();

        log.warn(
                "HTTP method '{}' is not supported. Supported methods: {}",
                method,
                supportedMethods
        );

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "HTTP method '" + method
                        + "' is not allowed for this endpoint",
                "Supported method(s): " + supportedMethods,
                null
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        ConstraintViolation<?> violation =
                ex.getConstraintViolations()
                        .stream()
                        .findFirst()
                        .orElse(null);

        String message = violation != null
                ? violation.getMessage()
                : "Invalid request parameter";

        String propertyPath = violation != null
                ? violation.getPropertyPath().toString()
                : "request";

        log.warn(
                "Constraint violation for '{}': {}",
                propertyPath,
                message
        );

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.BAD_REQUEST.value(),
                message,
                "Invalid value for '" + propertyPath + "'",
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse> handleNoResourceFound(
            NoResourceFoundException ex
    ) {
        log.warn(
                "Endpoint not found: {} {}",
                ex.getHttpMethod(),
                ex.getResourcePath()
        );

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.NOT_FOUND.value(),
                "The requested endpoint does not exist",
                "Resource not found",
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnexpectedException(
            Exception ex
    ) {
        log.error(
                "An unexpected error occurred while processing the request",
                ex
        );

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Something went wrong",
                "An unexpected error occurred while processing the request",
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    private String buildReadableJsonError(Throwable cause) {
        if (cause == null) {
            return "Request body is missing or contains malformed JSON";
        }

        if (cause instanceof InvalidFormatException exception) {
            return buildInvalidFormatMessage(exception);
        }

        if (cause instanceof MismatchedInputException exception) {
            return buildMismatchedInputMessage(exception);
        }

        return buildMalformedJsonMessage(cause.getMessage());
    }

    private String buildInvalidFormatMessage(
            InvalidFormatException exception
    ) {
        String fieldName = extractJsonFieldPath(exception);

        Object rejectedValue = exception.getValue();
        Class<?> targetType = exception.getTargetType();

        /*
         * Invalid enum value.
         *
         * Example:
         * "rentPaymentFrequency": "YEARLY"
         */
        if (targetType != null && targetType.isEnum()) {
            String acceptedValues = Arrays.stream(
                            targetType.getEnumConstants()
                    )
                    .map(enumValue ->
                            ((Enum<?>) enumValue).name()
                    )
                    .collect(Collectors.joining(", "));

            return String.format(
                    "Invalid value '%s' for field '%s'. "
                            + "Accepted values are: %s",
                    rejectedValue,
                    fieldName,
                    acceptedValues
            );
        }

        /*
         * Invalid UUID.
         *
         * Example:
         * "propertyTypeId": "not-a-uuid"
         */
        if (UUID.class.equals(targetType)) {
            return String.format(
                    "Invalid value '%s' for field '%s'. "
                            + "A valid UUID is required",
                    rejectedValue,
                    fieldName
            );
        }

        String expectedType =
                friendlyTypeName(targetType);

        return String.format(
                "Invalid value '%s' for field '%s'. Expected %s",
                rejectedValue,
                fieldName,
                expectedType
        );
    }

    private String buildMismatchedInputMessage(
            MismatchedInputException exception
    ) {
        String originalMessage = exception.getMessage();

        /*
         * Empty request body.
         */
        if (
                originalMessage != null
                        && (
                        originalMessage.contains(
                                "No content to map"
                        )
                                || originalMessage.contains(
                                "end-of-input"
                        )
                )
        ) {
            return "Request body is required";
        }

        String fieldName = extractJsonFieldPath(exception);

        Class<?> targetType = exception.getTargetType();

        String expectedType =
                friendlyTypeName(targetType);

        if ("requestBody".equals(fieldName)) {
            return "Invalid request body. Expected "
                    + expectedType;
        }

        return String.format(
                "Invalid value for field '%s'. Expected %s",
                fieldName,
                expectedType
        );
    }

    private String extractJsonFieldPath(
            MismatchedInputException exception
    ) {
        String pathReference = exception.getPathReference();

        if (
                pathReference == null
                        || pathReference.isBlank()
        ) {
            return "requestBody";
        }

        Matcher matcher =
                JACKSON_PATH_PATTERN.matcher(pathReference);

        StringBuilder path = new StringBuilder();

        while (matcher.find()) {
            String propertyName = matcher.group(1);
            String index = matcher.group(2);

            if (propertyName != null) {
                if (!path.isEmpty()) {
                    path.append(".");
                }

                path.append(propertyName);
            } else if (index != null) {
                path.append("[")
                        .append(index)
                        .append("]");
            }
        }

        return path.isEmpty()
                ? "requestBody"
                : path.toString();
    }

    private String friendlyTypeName(Class<?> targetType) {
        if (targetType == null) {
            return "a value of the correct type";
        }

        if (
                targetType.equals(Integer.class)
                        || targetType.equals(int.class)
                        || targetType.equals(Long.class)
                        || targetType.equals(long.class)
                        || targetType.equals(Double.class)
                        || targetType.equals(double.class)
                        || targetType.equals(Float.class)
                        || targetType.equals(float.class)
                        || Number.class.isAssignableFrom(targetType)
        ) {
            return "a valid number";
        }

        if (
                targetType.equals(Boolean.class)
                        || targetType.equals(boolean.class)
        ) {
            return "true or false";
        }

        if (
                Collection.class.isAssignableFrom(targetType)
                        || targetType.isArray()
        ) {
            return "a JSON array";
        }

        if (Map.class.isAssignableFrom(targetType)) {
            return "a JSON object";
        }

        if (String.class.equals(targetType)) {
            return "text";
        }

        if (UUID.class.equals(targetType)) {
            return "a valid UUID";
        }

        return "a value of type "
                + targetType.getSimpleName();
    }

    private String buildMalformedJsonMessage(
            String originalMessage
    ) {
        if (
                originalMessage == null
                        || originalMessage.isBlank()
        ) {
            return "Request body contains malformed JSON";
        }

        String cleanedMessage = originalMessage;

        int sourcePosition =
                cleanedMessage.indexOf(" at [Source:");

        if (sourcePosition >= 0) {
            cleanedMessage =
                    cleanedMessage.substring(
                            0,
                            sourcePosition
                    );
        }

        int referenceChainPosition =
                cleanedMessage.indexOf(
                        " (through reference chain:"
                );

        if (referenceChainPosition >= 0) {
            cleanedMessage =
                    cleanedMessage.substring(
                            0,
                            referenceChainPosition
                    );
        }

        cleanedMessage = cleanedMessage
                .replaceAll(
                        "`[^`]+`",
                        "the expected type"
                )
                .trim();

        if (cleanedMessage.isBlank()) {
            return "Request body contains malformed JSON";
        }

        return "Malformed JSON: " + cleanedMessage;
    }
}
