package com.project_x.core.exception;

import com.project_x.core.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerRequestBindingTests {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingRequestParameterReturnsBadRequest() {
        MissingServletRequestParameterException exception =
                mock(MissingServletRequestParameterException.class);
        when(exception.getParameterName()).thenReturn("fileName");

        ResponseEntity<ApiResponse> response =
                handler.handleMissingRequestParameter(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Required request parameter 'fileName' is missing",
                response.getBody().getMessage()
        );
    }

    @Test
    void invalidRequestParameterTypeReturnsBadRequest() {
        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("fileSize");
        when(exception.getValue()).thenReturn("large");
        doReturn(long.class).when(exception).getRequiredType();

        ResponseEntity<ApiResponse> response =
                handler.handleRequestParameterTypeMismatch(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Invalid value for request parameter 'fileSize'. Expected a valid number",
                response.getBody().getMessage()
        );
    }
}
