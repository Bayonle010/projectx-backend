package com.project_x.core.response;

import lombok.Data;

import java.time.Instant;

@Data
public class ApiResponse {
    private boolean status;
    private int statusCode;
    private String message;
    private String details;
    private Object data;
    private Object metadata;
    private Instant timeStamp;

    public ApiResponse(boolean status, int statusCode, String message, String details ,  Object data, Object metadata, Instant timeStamp) {
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.details = details;
        this.data = data;
        this.metadata = metadata;
        this.timeStamp = timeStamp;
    }

}