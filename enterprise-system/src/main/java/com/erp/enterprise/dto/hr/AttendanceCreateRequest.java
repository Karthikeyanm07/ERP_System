package com.erp.enterprise.dto.hr;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCreateRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private LocalTime clockIn;
    private LocalTime clockOut;

    @NotNull(message = "Status is required")
    private String status;  // PRESENT, ABSENT, HALF_DAY, LEAVE

    private String remarks;
}