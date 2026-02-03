package com.erp.enterprise.service.hr.impl;

import com.erp.enterprise.dto.hr.LeaveApprovalRequest;
import com.erp.enterprise.dto.hr.LeaveRequestCreateRequest;
import com.erp.enterprise.dto.hr.LeaveRequestDTO;
import com.erp.enterprise.entity.hr.Employee;
import com.erp.enterprise.entity.hr.LeaveRequest;
import com.erp.enterprise.entity.hr.LeaveType;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.hr.EmployeeRepository;
import com.erp.enterprise.repository.hr.LeaveRequestRepository;
import com.erp.enterprise.repository.hr.LeaveTypeRepository;
import com.erp.enterprise.service.hr.LeaveRequestService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Leave Request Service Implementation
 *
 * Business Logic:
 * - Employees submit leave requests
 * - Validates leave balance (days remaining)
 * - Checks for overlapping leaves
 * - Managers approve/reject requests
 * - Approved leaves reduce balance
 */
@Service
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    @Autowired
    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository,
                                   EmployeeRepository employeeRepository,
                                   LeaveTypeRepository leaveTypeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @Override
    public LeaveRequestDTO submitLeaveRequest(LeaveRequestCreateRequest request) {
        // Validate employee
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee", "id", request.getEmployeeId()));

        // Validate leave type
        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LeaveType", "id", request.getLeaveTypeId()));

        // Business Logic: Validate dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException(
                    "Start date cannot be after end date",
                    "INVALID_DATE_RANGE");
        }

        // Business Logic: Check for overlapping approved leaves
        List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findOverlappingLeaves(
                request.getEmployeeId(),
                request.getStartDate(),
                request.getEndDate());

        if (!overlappingLeaves.isEmpty()) {
            throw new BusinessException(
                    "Leave request overlaps with existing approved leave",
                    "OVERLAPPING_LEAVE");
        }

        // Business Logic: Check leave balance for current year
        int currentYear = request.getStartDate().getYear();
        int leavesTaken = leaveRequestRepository.countApprovedLeavesByTypeAndYear(
                request.getEmployeeId(),
                request.getLeaveTypeId(),
                currentYear);

        int remainingLeaves = leaveType.getDaysAllowed() - leavesTaken;

        if (request.getDaysCount() > remainingLeaves) {
            throw new BusinessException(
                    String.format("Insufficient leave balance. Requested: %d days, Available: %d days",
                            request.getDaysCount(), remainingLeaves),
                    "INSUFFICIENT_LEAVE_BALANCE");
        }

        // Create leave request
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setDaysCount(request.getDaysCount());
        leaveRequest.setReason(request.getReason());
        leaveRequest.setStatus("PENDING");

        LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        return DtoMapper.toLeaveRequestDTO(savedLeaveRequest);
    }

    @Override
    public LeaveRequestDTO getLeaveRequestById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        return DtoMapper.toLeaveRequestDTO(leaveRequest);
    }

    @Override
    public List<LeaveRequestDTO> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findAll();

        return leaveRequests.stream()
                .map(DtoMapper::toLeaveRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDTO> getLeaveRequestsByEmployee(Long employeeId) {
        // Validate employee
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        List<LeaveRequest> leaveRequests =
                leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);

        return leaveRequests.stream()
                .map(DtoMapper::toLeaveRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDTO> getLeaveRequestsByStatus(String status) {
        // Validate status
        if (!isValidLeaveStatus(status)) {
            throw new BusinessException(
                    "Invalid leave status: " + status,
                    "INVALID_LEAVE_STATUS");
        }

        List<LeaveRequest> leaveRequests =
                leaveRequestRepository.findByStatusOrderByCreatedAtDesc(status);

        return leaveRequests.stream()
                .map(DtoMapper::toLeaveRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDTO> getPendingLeaveRequestsByEmployee(Long employeeId) {
        // Validate employee
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        List<LeaveRequest> leaveRequests =
                leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, "PENDING");

        return leaveRequests.stream()
                .map(DtoMapper::toLeaveRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveRequestDTO processLeaveRequest(Long id, LeaveApprovalRequest approvalRequest) {
        // Find leave request
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        // Business Logic: Can only process pending requests
        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new BusinessException(
                    "Can only process pending leave requests",
                    "ALREADY_PROCESSED");
        }

        // Validate approver - optional for HR users who may not have employee records
        Employee approver = null;
        if (approvalRequest.getApprovedById() != null) {
            approver = employeeRepository.findById(approvalRequest.getApprovedById())
                    .orElse(null); // Don't throw if not found - HR users may not have employee records
        }

        // Validate status
        if (!approvalRequest.getStatus().equals("APPROVED") &&
                !approvalRequest.getStatus().equals("REJECTED")) {
            throw new BusinessException(
                    "Status must be APPROVED or REJECTED",
                    "INVALID_APPROVAL_STATUS");
        }

        // Update leave request
        leaveRequest.setStatus(approvalRequest.getStatus());
        if (approver != null) {
            leaveRequest.setApprovedBy(approver);
        }
        leaveRequest.setApprovedAt(LocalDateTime.now());

        LeaveRequest updatedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        return DtoMapper.toLeaveRequestDTO(updatedLeaveRequest);
    }

    @Override
    public LeaveRequestDTO cancelLeaveRequest(Long id) {
        // Find leave request
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        // Business Logic: Can only cancel pending requests
        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new BusinessException(
                    "Can only cancel pending leave requests",
                    "CANNOT_CANCEL");
        }

        // Mark as rejected
        leaveRequest.setStatus("REJECTED");

        LeaveRequest updatedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        return DtoMapper.toLeaveRequestDTO(updatedLeaveRequest);
    }

    @Override
    public void deleteLeaveRequest(Long id) {
        // Check if leave request exists
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        // Business Logic: Can only delete pending or rejected requests
        if ("APPROVED".equals(leaveRequest.getStatus())) {
            throw new BusinessException(
                    "Cannot delete approved leave requests",
                    "CANNOT_DELETE_APPROVED");
        }

        leaveRequestRepository.delete(leaveRequest);
    }

    // Helper method to validate leave status
    private boolean isValidLeaveStatus(String status) {
        return status != null &&
                (status.equals("PENDING") ||
                        status.equals("APPROVED") ||
                        status.equals("REJECTED"));
    }
}