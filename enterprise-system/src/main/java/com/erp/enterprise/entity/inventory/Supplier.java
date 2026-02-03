package com.erp.enterprise.entity.inventory;

import com.erp.enterprise.entity.hr.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Supplier Entity
 *
 * Business Logic:
 * - Vendors who supply products to the organization
 * - Contact information for procurement
 * - Active/Inactive status for vendor management
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    @NotBlank(message = "Supplier code is required")
    @Size(max = 20, message = "Supplier code must not exceed 20 characters")
    @Column(name = "supplier_code", unique = true, nullable = false, length = 20)
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 100, message = "Supplier name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Email(message = "Email should be valid")
    @Column(name = "email", length = 100)
    private String email;

    @Pattern(regexp = "^[0-9]{10,20}$", message = "Phone number should be valid")
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Size(max = 50, message = "City must not exceed 50 characters")
    @Column(name = "city", length = 50)
    private String city;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "is_active")
    private Boolean isActive = true;
}