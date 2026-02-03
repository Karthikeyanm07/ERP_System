package com.erp.enterprise.dto.sales;

import java.time.LocalDateTime;

/**
 * Response DTO for Customer List View
 * 
 * Purpose: Lightweight DTO for GET /api/customers (list endpoint)
 * Security: Excludes sensitive financial fields
 * 
 * Fields included:
 * - Basic identification (id, code, name, contactPerson)
 * - Contact info (email, phone, city)
 * - Status (isActive)
 * 
 * Fields EXCLUDED:
 * - creditLimit (sensitive financial)
 * - outstandingBalance (sensitive financial)
 * - address, country (not needed in table)
 */
public class CustomerListResponse {

    private Long id;
    private String customerCode;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String city;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CustomerListResponse() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
