package com.project_x.core.exception;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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




}
