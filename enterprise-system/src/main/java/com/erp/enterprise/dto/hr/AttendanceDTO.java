package com.erp.enterprise.dto.hr;

import com.erp.enterprise.dto.BaseDTO;
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
public class AttendanceDTO extends BaseDTO {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String employeeCode;
    private String employeeName;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private LocalTime clockIn;
    private LocalTime clockOut;

    @NotNull(message = "Status is required")
    private String status;  // PRESENT, ABSENT, HALF_DAY, LEAVE

    private String remarks;
    private Double workHours;  // Calculated field
}