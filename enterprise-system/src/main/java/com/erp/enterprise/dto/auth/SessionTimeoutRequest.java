package com.erp.enterprise.dto.auth;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating user session timeout preference.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionTimeoutRequest {

    @NotNull(message = "Session timeout is required")
    @Min(value = 15, message = "Minimum 15 minutes")
    @Max(value = 480, message = "Maximum 480 minutes (8 hours)")
    private Integer sessionTimeoutMinutes;
}
