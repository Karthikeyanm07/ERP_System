package com.erp.enterprise.controller.hr;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.hr.LeaveTypeDTO;
import com.erp.enterprise.service.hr.LeaveTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Leave Type Controller
 * <p>
 * Base URL: /api/leave-types
 * <p>
 * Available endpoints:
 * - GET    /api/leave-types           -> Get all leave types
 * - GET    /api/leave-types/{id}      -> Get by ID
 * - POST   /api/leave-types           -> Create leave type
 * - PUT    /api/leave-types/{id}      -> Update leave type
 * - DELETE /api/leave-types/{id}      -> Delete leave type
 */
@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @Autowired
    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    /**
     * GET /api/leave-types
     * Get all leave types
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveTypeDTO>>> getAllLeaveTypes() {
        List<LeaveTypeDTO> leaveTypes = leaveTypeService.getAllLeaveTypes();
        return ResponseEntity.ok(
                ApiResponse.success("Leave types retrieved successfully", leaveTypes)
        );
    }

    /**
     * GET /api/leave-types/{id}
     * Get leave type by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveTypeDTO>> getLeaveTypeById(@PathVariable Long id) {
        LeaveTypeDTO leaveType = leaveTypeService.getLeaveTypeById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Leave type retrieved successfully", leaveType)
        );
    }

    /**
     * POST /api/leave-types
     * Create new leave type
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LeaveTypeDTO>> createLeaveType(
            @Valid @RequestBody LeaveTypeDTO leaveTypeDTO) {

        LeaveTypeDTO createdLeaveType = leaveTypeService.createLeaveType(leaveTypeDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave type created successfully", createdLeaveType));
    }

    /**
     * PUT /api/leave-types/{id}
     * Update leave type
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveTypeDTO>> updateLeaveType(
            @PathVariable Long id,
            @Valid @RequestBody LeaveTypeDTO leaveTypeDTO) {

        LeaveTypeDTO updatedLeaveType = leaveTypeService.updateLeaveType(id, leaveTypeDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Leave type updated successfully", updatedLeaveType)
        );
    }

    /**
     * DELETE /api/leave-types/{id}
     * Delete leave type
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveType(@PathVariable Long id) {
        leaveTypeService.deleteLeaveType(id);
        return ResponseEntity.ok(
                ApiResponse.success("Leave type deleted successfully", null)
        );
    }
}