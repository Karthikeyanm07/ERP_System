package com.erp.enterprise.service.hr;

import com.erp.enterprise.dto.hr.LeaveApprovalRequest;
import com.erp.enterprise.dto.hr.LeaveRequestCreateRequest;
import com.erp.enterprise.dto.hr.LeaveRequestDTO;

import java.util.List;

/**
 * Leave Request Service Interface
 */
public interface LeaveRequestService {

    // Submit leave request
    LeaveRequestDTO submitLeaveRequest(LeaveRequestCreateRequest request);

    // Get leave request by ID
    LeaveRequestDTO getLeaveRequestById(Long id);

    // Get all leave requests
    List<LeaveRequestDTO> getAllLeaveRequests();

    // Get leave requests by employee
    List<LeaveRequestDTO> getLeaveRequestsByEmployee(Long employeeId);

    // Get leave requests by status
    List<LeaveRequestDTO> getLeaveRequestsByStatus(String status);

    // Get pending leave requests for employee
    List<LeaveRequestDTO> getPendingLeaveRequestsByEmployee(Long employeeId);

    // Approve or reject leave request
    LeaveRequestDTO processLeaveRequest(Long id, LeaveApprovalRequest approvalRequest);

    // Cancel leave request (by employee)
    LeaveRequestDTO cancelLeaveRequest(Long id);

    // Delete leave request
    void deleteLeaveRequest(Long id);
}