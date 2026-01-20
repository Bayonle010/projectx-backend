package com.project_x.core.response;

import java.time.Instant;

public class ResponseUtil {
    public static  ApiResponse  success (int statusCode, String message, String details, Object data , Object metadata){
        return new ApiResponse(true, statusCode, message, details, data, metadata ,  Instant.now());
    }

    public static ApiResponse error (int statusCode, String message , String details, Object data){
        return new ApiResponse(false, statusCode, message, details, data, null, Instant.now());
    }
}
