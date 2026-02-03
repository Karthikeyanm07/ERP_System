package com.erp.enterprise.controller.hr;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.hr.LeaveApprovalRequest;
import com.erp.enterprise.dto.hr.LeaveRequestCreateRequest;
import com.erp.enterprise.dto.hr.LeaveRequestDTO;
import com.erp.enterprise.service.hr.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Leave Request Controller
 * <p>
 * Base URL: /api/leave-requests
 * <p>
 * Available endpoints:
 * - GET    /api/leave-requests                        -> Get all leave requests
 * - GET    /api/leave-requests/{id}                   -> Get by ID
 * - GET    /api/leave-requests/employee/{employeeId}  -> Get by employee
 * - GET    /api/leave-requests/status/{status}        -> Get by status
 * - GET    /api/leave-requests/employee/{employeeId}/pending -> Get pending
 * - POST   /api/leave-requests                        -> Submit leave request
 * - PUT    /api/leave-requests/{id}/process           -> Approve/Reject
 * - PUT    /api/leave-requests/{id}/cancel            -> Cancel request
 * - DELETE /api/leave-requests/{id}                   -> Delete request
 */
@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    /**
     * GET /api/leave-requests
     * Get all leave requests
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> getAllLeaveRequests() {
        List<LeaveRequestDTO> leaveRequests = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(
                ApiResponse.success("Leave requests retrieved successfully", leaveRequests)
        );
    }

    /**
     * GET /api/leave-requests/{id}
     * Get leave request by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> getLeaveRequestById(@PathVariable Long id) {
        LeaveRequestDTO leaveRequest = leaveRequestService.getLeaveRequestById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Leave request retrieved successfully", leaveRequest)
        );
    }

    /**
     * GET /api/leave-requests/employee/{employeeId}
     * Get all leave requests for specific employee
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> getLeaveRequestsByEmployee(
            @PathVariable Long employeeId) {

        List<LeaveRequestDTO> leaveRequests =
                leaveRequestService.getLeaveRequestsByEmployee(employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Employee leave requests retrieved successfully", leaveRequests)
        );
    }

    /**
     * GET /api/leave-requests/status/{status}
     * Get leave requests by status
     * <p>
     * Valid statuses: PENDING, APPROVED, REJECTED
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> getLeaveRequestsByStatus(
            @PathVariable String status) {

        List<LeaveRequestDTO> leaveRequests = leaveRequestService.getLeaveRequestsByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success("Leave requests by status retrieved successfully", leaveRequests)
        );
    }

    /**
     * GET /api/leave-requests/employee/{employeeId}/pending
     * Get pending leave requests for employee
     */
    @GetMapping("/employee/{employeeId}/pending")
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> getPendingLeaveRequestsByEmployee(
            @PathVariable Long employeeId) {

        List<LeaveRequestDTO> leaveRequests =
                leaveRequestService.getPendingLeaveRequestsByEmployee(employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Pending leave requests retrieved successfully", leaveRequests)
        );
    }

    /**
     * POST /api/leave-requests
     * Submit new leave request
     * <p>
     * Business Logic: Validates leave balance and checks for overlaps
     */
    @PostMapping
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> submitLeaveRequest(
            @Valid @RequestBody LeaveRequestCreateRequest request) {

        LeaveRequestDTO leaveRequest = leaveRequestService.submitLeaveRequest(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave request submitted successfully", leaveRequest));
    }

    /**
     * PUT /api/leave-requests/{id}/process
     * Approve or reject leave request
     * <p>
     * Body: { "approvedById": 2, "status": "APPROVED" or "REJECTED", "remarks": "..." }
     */
    @PutMapping("/{id}/process")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> processLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalRequest approvalRequest) {

        LeaveRequestDTO leaveRequest = leaveRequestService.processLeaveRequest(id, approvalRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Leave request processed successfully", leaveRequest)
        );
    }

    /**
     * PUT /api/leave-requests/{id}/cancel
     * Cancel leave request (by employee)
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> cancelLeaveRequest(@PathVariable Long id) {
        LeaveRequestDTO leaveRequest = leaveRequestService.cancelLeaveRequest(id);
        return ResponseEntity.ok(
                ApiResponse.success("Leave request cancelled successfully", leaveRequest)
        );
    }

    /**
     * DELETE /api/leave-requests/{id}
     * Delete leave request
     * <p>
     * Business Logic: Cannot delete approved requests
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveRequest(@PathVariable Long id) {
        leaveRequestService.deleteLeaveRequest(id);
        return ResponseEntity.ok(
                ApiResponse.success("Leave request deleted successfully", null)
        );
    }
}