package com.erp.enterprise.service.hr;

import com.erp.enterprise.dto.hr.LeaveTypeDTO;
import java.util.List;

/**
 * Leave Type Service Interface
 */
public interface LeaveTypeService {

    // Create leave type
    LeaveTypeDTO createLeaveType(LeaveTypeDTO leaveTypeDTO);

    // Get leave type by ID
    LeaveTypeDTO getLeaveTypeById(Long id);

    // Get all leave types
    List<LeaveTypeDTO> getAllLeaveTypes();

    // Update leave type
    LeaveTypeDTO updateLeaveType(Long id, LeaveTypeDTO leaveTypeDTO);

    // Delete leave type
    void deleteLeaveType(Long id);
}