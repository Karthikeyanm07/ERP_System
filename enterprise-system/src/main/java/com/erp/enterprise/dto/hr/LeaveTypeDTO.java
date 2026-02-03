package com.erp.enterprise.dto.hr;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeDTO {

    private Long id;

    @NotBlank(message = "Leave type name is required")
    @Size(max = 50, message = "Leave type name must not exceed 50 characters")
    private String name;

    private String description;

    @NotNull(message = "Days allowed is required")
    @Min(value = 1, message = "Days allowed must be at least 1")
    private Integer daysAllowed;
}