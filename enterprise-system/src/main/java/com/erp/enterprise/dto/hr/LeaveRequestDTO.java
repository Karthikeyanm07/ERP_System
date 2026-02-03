package com.erp.enterprise.dto.hr;

import com.erp.enterprise.dto.BaseDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO extends BaseDTO {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String employeeCode;
    private String employeeName;

    @NotNull(message = "Leave type ID is required")
    private Long leaveTypeId;

    private String leaveTypeName;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Days count is required")
    @Min(value = 1, message = "Days count must be at least 1")
    private Integer daysCount;

    private String reason;
    private String status;  // PENDING, APPROVED, REJECTED

    private Long approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
}