package com.erp.enterprise.controller.hr;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.hr.AttendanceCreateRequest;
import com.erp.enterprise.dto.hr.AttendanceDTO;
import com.erp.enterprise.service.hr.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Attendance Controller
 * <p>
 * Base URL: /api/attendance
 * <p>
 * Available endpoints:
 * - GET    /api/attendance                           -> Get all attendance
 * - GET    /api/attendance/{id}                      -> Get by ID
 * - GET    /api/attendance/employee/{employeeId}     -> Get by employee
 * - GET    /api/attendance/date/{date}               -> Get by date
 * - GET    /api/attendance/employee/{employeeId}/range -> Get by date range
 * - GET    /api/attendance/status/{status}           -> Get by status
 * - POST   /api/attendance                           -> Mark attendance
 * - POST   /api/attendance/clock-in                  -> Clock in
 * - POST   /api/attendance/clock-out                 -> Clock out
 * - PUT    /api/attendance/{id}                      -> Update attendance
 * - DELETE /api/attendance/{id}                      -> Delete attendance
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * GET /api/attendance
     * Get all attendance records
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAllAttendance() {
        List<AttendanceDTO> attendance = attendanceService.getAllAttendance();
        return ResponseEntity.ok(
                ApiResponse.success("Attendance records retrieved successfully", attendance)
        );
    }

    /**
     * GET /api/attendance/{id}
     * Get attendance by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceDTO>> getAttendanceById(@PathVariable Long id) {
        AttendanceDTO attendance = attendanceService.getAttendanceById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Attendance retrieved successfully", attendance)
        );
    }

    /**
     * GET /api/attendance/employee/{employeeId}
     * Get all attendance for specific employee
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendanceByEmployee(@PathVariable Long employeeId) {

        List<AttendanceDTO> attendance = attendanceService.getAttendanceByEmployee(employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Employee attendance retrieved successfully", attendance)
        );
    }
    /**
     * GET /api/attendance/date/{date}
     * Get attendance for specific date
     *
     * Example: /api/attendance/date/2026-01-15
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AttendanceDTO> attendance = attendanceService.getAttendanceByDate(date);
        return ResponseEntity.ok(
                ApiResponse.success("Attendance for date retrieved successfully", attendance)
        );
    }

    /**
     * GET /api/attendance/employee/{employeeId}/range
     * Get attendance for employee in date range
     *
     * Query params: startDate, endDate
     * Example: /api/attendance/employee/1/range?startDate=2026-01-01&endDate=2026-01-31
     */
    @GetMapping("/employee/{employeeId}/range")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendanceByEmployeeAndDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<AttendanceDTO> attendance = attendanceService.getAttendanceByEmployeeAndDateRange(
                employeeId, startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success("Attendance range retrieved successfully", attendance)
        );
    }

    /**
     * GET /api/attendance/status/{status}
     * Get attendance by status
     *
     * Valid statuses: PRESENT, ABSENT, HALF_DAY, LEAVE
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendanceByStatus(
            @PathVariable String status) {

        List<AttendanceDTO> attendance = attendanceService.getAttendanceByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success("Attendance by status retrieved successfully", attendance)
        );
    }

    /**
     * POST /api/attendance
     * Mark attendance for employee
     */
    @PostMapping
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> markAttendance(
            @Valid @RequestBody AttendanceCreateRequest request) {

        AttendanceDTO attendance = attendanceService.markAttendance(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance marked successfully", attendance));
    }

    /**
     * POST /api/attendance/clock-in
     * Clock in for today
     *
     * Body: { "employeeId": 1, "date": "2026-01-15" }
     */
    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> clockIn(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        AttendanceDTO attendance = attendanceService.clockIn(employeeId, date);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Clocked in successfully", attendance));
    }

    /**
     * POST /api/attendance/clock-out
     * Clock out for today
     *
     * Body: { "employeeId": 1, "date": "2026-01-15" }
     */
    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> clockOut(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        AttendanceDTO attendance = attendanceService.clockOut(employeeId, date);
        return ResponseEntity.ok(
                ApiResponse.success("Clocked out successfully", attendance)
        );
    }

    /**
     * PUT /api/attendance/{id}
     * Update attendance record
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceDTO attendanceDTO) {

        AttendanceDTO updatedAttendance = attendanceService.updateAttendance(id, attendanceDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Attendance updated successfully", updatedAttendance)
        );
    }

    /**
     * DELETE /api/attendance/{id}
     * Delete attendance record
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(
                ApiResponse.success("Attendance deleted successfully", null)
        );
    }
}