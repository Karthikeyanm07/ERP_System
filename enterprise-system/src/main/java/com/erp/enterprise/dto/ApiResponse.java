package com.erp.enterprise.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

// Standardized success response wrapper
// Business Logic: Frontend receives consistent structure for all successful API calls
// This makes it easy to handle responses in a generic way on the frontend
public class ApiResponse<T> {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private boolean success;      // Always true for successful responses
    private String message;       // Success message
    private T data;              // The actual response data (employee, product, list, etc.)

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }

    public ApiResponse(String message, T data) {
        this();
        this.message = message;
        this.data = data;
    }

    public ApiResponse(T data) {
        this();
        this.data = data;
    }

    // Static factory methods for easy creation
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}