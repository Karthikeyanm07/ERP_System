package com.erp.enterprise.dto.hr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalRequest {

    @NotNull(message = "Approved by employee ID is required")
    private Long approvedById;

    @NotBlank(message = "Status is required")
    private String status;  // APPROVED or REJECTED

    private String remarks;
}