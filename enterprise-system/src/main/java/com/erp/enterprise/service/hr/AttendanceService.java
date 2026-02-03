package com.erp.enterprise.service.hr;

import com.erp.enterprise.dto.hr.AttendanceCreateRequest;
import com.erp.enterprise.dto.hr.AttendanceDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Attendance Service Interface
 */
public interface AttendanceService {

    // Mark attendance for an employee
    AttendanceDTO markAttendance(AttendanceCreateRequest request);

    // Get attendance by ID
    AttendanceDTO getAttendanceById(Long id);

    // Get all attendance records
    List<AttendanceDTO> getAllAttendance();

    // Get attendance for specific employee
    List<AttendanceDTO> getAttendanceByEmployee(Long employeeId);

    // Get attendance for specific date
    List<AttendanceDTO> getAttendanceByDate(LocalDate date);

    // Get attendance for employee in date range
    List<AttendanceDTO> getAttendanceByEmployeeAndDateRange(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    // Get attendance by status
    List<AttendanceDTO> getAttendanceByStatus(String status);

    // Update attendance
    AttendanceDTO updateAttendance(Long id, AttendanceDTO attendanceDTO);

    // Delete attendance record
    void deleteAttendance(Long id);

    // Clock in
    AttendanceDTO clockIn(Long employeeId, LocalDate date);

    // Clock out
    AttendanceDTO clockOut(Long employeeId, LocalDate date);
}