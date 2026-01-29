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
}
