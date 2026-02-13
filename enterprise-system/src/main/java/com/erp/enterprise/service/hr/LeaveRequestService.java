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
    @org.springframework.lang.NonNull LeaveRequestDTO submitLeaveRequest(@org.springframework.lang.NonNull LeaveRequestCreateRequest request);

    // Get leave request by ID
    @org.springframework.lang.NonNull LeaveRequestDTO getLeaveRequestById(@org.springframework.lang.NonNull Long id);

    // Get all leave requests
    List<LeaveRequestDTO> getAllLeaveRequests();

    // Get leave requests by employee
    List<LeaveRequestDTO> getLeaveRequestsByEmployee(@org.springframework.lang.NonNull Long employeeId);

    // Get leave requests by status
    List<LeaveRequestDTO> getLeaveRequestsByStatus(@org.springframework.lang.NonNull String status);

    // Get pending leave requests for employee
    List<LeaveRequestDTO> getPendingLeaveRequestsByEmployee(@org.springframework.lang.NonNull Long employeeId);

    // Approve or reject leave request
    @org.springframework.lang.NonNull LeaveRequestDTO processLeaveRequest(@org.springframework.lang.NonNull Long id, @org.springframework.lang.NonNull LeaveApprovalRequest approvalRequest);

    // Cancel leave request (by employee)
    @org.springframework.lang.NonNull LeaveRequestDTO cancelLeaveRequest(@org.springframework.lang.NonNull Long id);

    // Delete leave request
    void deleteLeaveRequest(@org.springframework.lang.NonNull Long id);
}