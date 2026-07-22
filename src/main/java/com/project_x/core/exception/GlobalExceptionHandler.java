package com.project_x.core.exception;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse> handleValidationsException(MethodArgumentNotValidException ex){
        log.error("An Unexpected error occurred in Method Argument : {}", ex.getMessage());

        String fieldName = ex.getBindingResult().getFieldError() != null ?
                ex.getBindingResult().getFieldError().getField() : "";

        String errorMessage = ex.getBindingResult().getFieldError() != null ?
                ex.getBindingResult().getFieldError().getDefaultMessage() : "Validation error";


        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.BAD_REQUEST.value(), "error with " +  fieldName, errorMessage, null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException e){
        log.error("AccessDenied  {} ", e.getMessage());
        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.FORBIDDEN.value(), e.getMessage(), "Access denied", null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequestException(BadRequestException e){
        log.error("Bad request {} ", e.getMessage());
        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.BAD_REQUEST.value(),e.getMessage(),  "Bad Request" , null);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException e){
        log.error("Illegal argument exception error {} ", e.getMessage());
        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.BAD_REQUEST.value(), e.getMessage(), "Illegal Argument", null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<ApiResponse> handleInvalidCredentialsException(InvalidCredentialException e) {
        log.error("An unexpected error occurred {}", e.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.UNAUTHORIZED.value(), e.getMessage(),"Invalid Credential",  null);

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NetworkConnectivityException.class)
    public ResponseEntity<ApiResponse> handleNetworkConnectivityException(NetworkConnectivityException e) {
        log.error("Network connectivity issue: {}", e.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.SERVICE_UNAVAILABLE.value(), e.getMessage(),"InternetConnection Error.", null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found {}", ex.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.NOT_FOUND.value(),  ex.getMessage(), "resource not found",null);

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflictException(ConflictException ex){
        log.error("Conflict {}", ex.getMessage());

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.CONFLICT.value(),  ex.getMessage(), "conflict exist",null);

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtil.error(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        "Maximum upload size exceeded",
                        null
                ));
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleInvalidJson(
            HttpMessageNotReadableException ex
    ) {
        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request body",
                "Request body is missing, malformed, or contains invalid JSON",
                null
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex
    ) {
        String method = ex.getMethod();

        String supportedMethods = ex.getSupportedHttpMethods() == null
                ? "the correct HTTP method"
                : ex.getSupportedHttpMethods().toString();

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.METHOD_NOT_ALLOWED.value(), // 405
                "HTTP method '" + method + "' is not allowed for this endpoint",
                "Supported method(s): " + supportedMethods,
                null
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex) {

        String message = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Invalid request parameter");

        return ResponseEntity.badRequest().body(
                ResponseUtil.error(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        null,
                        null
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnexpectedException(Exception ex) {
        log.error("An unexpected error occurred while processing the request", ex);

        ApiResponse errorResponse = ResponseUtil.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "An unexpected error occurred while processing the request",
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
