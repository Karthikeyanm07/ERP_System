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
    @org.springframework.lang.NonNull AttendanceDTO markAttendance(@org.springframework.lang.NonNull AttendanceCreateRequest request);

    // Get attendance by ID
    @org.springframework.lang.NonNull AttendanceDTO getAttendanceById(@org.springframework.lang.NonNull Long id);

    // Get all attendance records
    List<AttendanceDTO> getAllAttendance();

    // Get attendance for specific employee
    List<AttendanceDTO> getAttendanceByEmployee(@org.springframework.lang.NonNull Long employeeId);

    // Get attendance for specific date
    List<AttendanceDTO> getAttendanceByDate(@org.springframework.lang.NonNull LocalDate date);

    // Get attendance for employee in date range
    List<AttendanceDTO> getAttendanceByEmployeeAndDateRange(
            @org.springframework.lang.NonNull Long employeeId, @org.springframework.lang.NonNull LocalDate startDate, @org.springframework.lang.NonNull LocalDate endDate);

    // Get attendance by status
    List<AttendanceDTO> getAttendanceByStatus(@org.springframework.lang.NonNull String status);

    // Update attendance
    @org.springframework.lang.NonNull AttendanceDTO updateAttendance(@org.springframework.lang.NonNull Long id, @org.springframework.lang.NonNull AttendanceDTO attendanceDTO);

    // Delete attendance record
    void deleteAttendance(@org.springframework.lang.NonNull Long id);

    // Clock in
    @org.springframework.lang.NonNull AttendanceDTO clockIn(@org.springframework.lang.NonNull Long employeeId, @org.springframework.lang.NonNull LocalDate date);

    // Clock out
    @org.springframework.lang.NonNull AttendanceDTO clockOut(@org.springframework.lang.NonNull Long employeeId, @org.springframework.lang.NonNull LocalDate date);
}