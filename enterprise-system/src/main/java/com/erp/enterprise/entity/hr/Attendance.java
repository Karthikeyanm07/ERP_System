package com.erp.enterprise.entity.hr;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Attendance Entity
 *
 * Business Logic: Tracks daily attendance of employees
 * - Clock-in and clock-out times
 * - Status: PRESENT, ABSENT, HALF_DAY, LEAVE
 * - One record per employee per day
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "date"})
})
public class Attendance extends BaseEntity {

    @NotNull(message = "Employee is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;

    @NotNull(message = "Date is required")
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "clock_in")
    private LocalTime clockIn;

    @Column(name = "clock_out")
    private LocalTime clockOut;

    @NotNull(message = "Status is required")
    @Column(name = "status", nullable = false, length = 20)
    private String status;  // PRESENT, ABSENT, HALF_DAY, LEAVE

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // Business method to calculate work hours
    public Double getWorkHours() {
        if (clockIn != null && clockOut != null) {
            long minutes = java.time.Duration.between(clockIn, clockOut).toMinutes();
            return minutes / 60.0;
        }
        return 0.0;
    }
}