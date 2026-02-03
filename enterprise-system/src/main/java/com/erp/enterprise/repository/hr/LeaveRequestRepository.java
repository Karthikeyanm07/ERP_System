package com.erp.enterprise.repository.hr;

import com.erp.enterprise.entity.hr.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Leave Request Repository
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // Find all leave requests for an employee
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    // Find leave requests by status
    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(String status);

    // Find leave requests by employee and status
    List<LeaveRequest> findByEmployeeIdAndStatusOrderByCreatedAtDesc(Long employeeId, String status);

    // Find pending leave requests for an employee
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, String status);

    // Check for overlapping leave requests
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
            "AND lr.status = 'APPROVED' " +
            "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaves(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Count leaves taken by employee for specific leave type
    @Query("SELECT COALESCE(SUM(lr.daysCount), 0) FROM LeaveRequest lr " +
            "WHERE lr.employee.id = :employeeId " +
            "AND lr.leaveType.id = :leaveTypeId " +
            "AND lr.status = 'APPROVED' " +
            "AND YEAR(lr.startDate) = :year")
    int countApprovedLeavesByTypeAndYear(
            @Param("employeeId") Long employeeId,
            @Param("leaveTypeId") Long leaveTypeId,
            @Param("year") int year);
}