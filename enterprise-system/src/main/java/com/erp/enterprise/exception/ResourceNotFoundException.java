package com.erp.enterprise.exception;

// This exception is thrown when a requested resource (employee, product, etc.) is not found
// Business Logic: Instead of returning null, we throw this exception for better error handling
public class ResourceNotFoundException extends RuntimeException {

    private String resourceName;  // e.g., "Employee", "Product"
    private String fieldName;     // e.g., "id", "email"
    private Object fieldValue;    // The actual value that was searched for

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // Getters
    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}