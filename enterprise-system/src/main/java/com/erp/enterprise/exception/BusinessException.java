package com.erp.enterprise.exception;

// This exception is for business logic violations
// Example: "Cannot delete employee with pending leaves"
// Business Logic: Separates business rule violations from technical errors
public class BusinessException extends RuntimeException {

    private String errorCode;  // Custom error codes for frontend handling

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
