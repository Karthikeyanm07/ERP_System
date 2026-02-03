package com.erp.enterprise.service.hr.impl;

import com.erp.enterprise.dto.hr.AttendanceCreateRequest;
import com.erp.enterprise.dto.hr.AttendanceDTO;
import com.erp.enterprise.entity.hr.Attendance;
import com.erp.enterprise.entity.hr.Employee;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.hr.AttendanceRepository;
import com.erp.enterprise.repository.hr.EmployeeRepository;
import com.erp.enterprise.service.hr.AttendanceService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Attendance Service Implementation
 *
 * Business Logic:
 * - One attendance record per employee per day
 * - Validates employee exists
 * - Prevents duplicate attendance for same date
 * - Calculates work hours automatically
 */
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public AttendanceDTO markAttendance(AttendanceCreateRequest request) {
        // Business Logic: Validate employee exists
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee", "id", request.getEmployeeId()));

        // Business Logic: Check if attendance already marked for this date
        if (attendanceRepository.existsByEmployeeIdAndDate(
                request.getEmployeeId(), request.getDate())) {
            throw new DuplicateResourceException(
                    "Attendance",
                    "employee and date",
                    employee.getFullName() + " on " + request.getDate());
        }

        // Business Logic: Validate status
        if (!isValidAttendanceStatus(request.getStatus())) {
            throw new BusinessException(
                    "Invalid attendance status: " + request.getStatus(),
                    "INVALID_ATTENDANCE_STATUS");
        }

        // Create attendance record
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(request.getDate());
        attendance.setClockIn(request.getClockIn());
        attendance.setClockOut(request.getClockOut());
        attendance.setStatus(request.getStatus());
        attendance.setRemarks(request.getRemarks());

        // Save
        Attendance savedAttendance = attendanceRepository.save(attendance);
        return DtoMapper.toAttendanceDTO(savedAttendance);
    }

    @Override
    public AttendanceDTO getAttendanceById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        return DtoMapper.toAttendanceDTO(attendance);
    }

    @Override
    public List<AttendanceDTO> getAllAttendance() {
        List<Attendance> attendanceList = attendanceRepository.findAll();

        return attendanceList.stream()
                .map(DtoMapper::toAttendanceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getAttendanceByEmployee(Long employeeId) {
        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        List<Attendance> attendanceList =
                attendanceRepository.findByEmployeeIdOrderByDateDesc(employeeId);

        return attendanceList.stream()
                .map(DtoMapper::toAttendanceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getAttendanceByDate(LocalDate date) {
        List<Attendance> attendanceList = attendanceRepository.findByDate(date);

        return attendanceList.stream()
                .map(DtoMapper::toAttendanceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getAttendanceByEmployeeAndDateRange(
            Long employeeId, LocalDate startDate, LocalDate endDate) {

        // Validate employee exists
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }

        // Business Logic: Validate date range
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    "Start date cannot be after end date",
                    "INVALID_DATE_RANGE");
        }

        List<Attendance> attendanceList =
                attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDate(
                        employeeId, startDate, endDate);

        return attendanceList.stream()
                .map(DtoMapper::toAttendanceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDTO> getAttendanceByStatus(String status) {
        // Validate status
        if (!isValidAttendanceStatus(status)) {
            throw new BusinessException(
                    "Invalid attendance status: " + status,
                    "INVALID_ATTENDANCE_STATUS");
        }

        List<Attendance> attendanceList = attendanceRepository.findByStatus(status);

        return attendanceList.stream()
                .map(DtoMapper::toAttendanceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceDTO updateAttendance(Long id, AttendanceDTO attendanceDTO) {
        // Find existing attendance
        Attendance existingAttendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        // Business Logic: Validate status if changed
        if (!isValidAttendanceStatus(attendanceDTO.getStatus())) {
            throw new BusinessException(
                    "Invalid attendance status: " + attendanceDTO.getStatus(),
                    "INVALID_ATTENDANCE_STATUS");
        }

        // Update fields
        existingAttendance.setClockIn(attendanceDTO.getClockIn());
        existingAttendance.setClockOut(attendanceDTO.getClockOut());
        existingAttendance.setStatus(attendanceDTO.getStatus());
        existingAttendance.setRemarks(attendanceDTO.getRemarks());

        // Save
        Attendance updatedAttendance = attendanceRepository.save(existingAttendance);
        return DtoMapper.toAttendanceDTO(updatedAttendance);
    }

    @Override
    public void deleteAttendance(Long id) {
        // Check if attendance exists
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        attendanceRepository.delete(attendance);
    }

    @Override
    public AttendanceDTO clockIn(Long employeeId, LocalDate date) {
        // Validate employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        // Business Logic: Check if already clocked in today
        if (attendanceRepository.existsByEmployeeIdAndDate(employeeId, date)) {
            throw new BusinessException(
                    "Attendance already marked for " + employee.getFullName() + " on " + date,
                    "ATTENDANCE_ALREADY_EXISTS");
        }

        // Create attendance with clock-in time
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(date);
        attendance.setClockIn(LocalTime.now());
        attendance.setStatus("PRESENT");

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return DtoMapper.toAttendanceDTO(savedAttendance);
    }

    @Override
    public AttendanceDTO clockOut(Long employeeId, LocalDate date) {
        // Find attendance record
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employeeId, date)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attendance",
                        "employee and date",
                        "Employee ID: " + employeeId + ", Date: " + date));

        // Business Logic: Check if already clocked out
        if (attendance.getClockOut() != null) {
            throw new BusinessException(
                    "Already clocked out for this date",
                    "ALREADY_CLOCKED_OUT");
        }

        // Set clock-out time
        attendance.setClockOut(LocalTime.now());

        Attendance updatedAttendance = attendanceRepository.save(attendance);
        return DtoMapper.toAttendanceDTO(updatedAttendance);
    }

    // Helper method to validate attendance status
    private boolean isValidAttendanceStatus(String status) {
        return status != null &&
                (status.equals("PRESENT") ||
                        status.equals("ABSENT") ||
                        status.equals("HALF_DAY") ||
                        status.equals("LEAVE"));
    }
}