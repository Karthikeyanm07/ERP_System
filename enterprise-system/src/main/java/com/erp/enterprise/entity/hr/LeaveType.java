package com.erp.enterprise.entity.hr;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Leave Type Entity
 *
 * Business Logic: Defines types of leaves available
 * Examples: Sick Leave (12 days), Casual Leave (10 days), Annual Leave (21 days)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leave_types")
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Leave type name is required")
    @Size(max = 50, message = "Leave type name must not exceed 50 characters")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Days allowed is required")
    @Min(value = 1, message = "Days allowed must be at least 1")
    @Column(name = "days_allowed", nullable = false)
    private Integer daysAllowed;

    public LeaveType(String name, String description, Integer daysAllowed) {
        this.name = name;
        this.description = description;
        this.daysAllowed = daysAllowed;
    }
}