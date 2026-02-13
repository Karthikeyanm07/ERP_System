package com.erp.enterprise.repository.hr;

import com.erp.enterprise.entity.hr.Attendance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Attendance Repository
 *
 * Business Logic: Query attendance records
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Override
    @EntityGraph(attributePaths = {"employee"})
    @org.springframework.lang.NonNull
    List<Attendance> findAll();

    // Check if attendance exists for employee on specific date
    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);

    // Find attendance for specific employee and date
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    // Find all attendance records for an employee
    List<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId);

    // Find attendance for employee in date range
    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDate(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    // Find all attendance on specific date
    List<Attendance> findByDate(LocalDate date);

    // Find attendance by date range
    List<Attendance> findByDateBetweenOrderByDate(LocalDate startDate, LocalDate endDate);

    // Find attendance by status
    List<Attendance> findByStatus(String status);

    // Count attendance records for employee by status
    long countByEmployeeIdAndStatus(Long employeeId, String status);

    // Get attendance statistics using custom query
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId " +
            "AND a.date BETWEEN :startDate AND :endDate AND a.status = :status")
    long countByEmployeeAndDateRangeAndStatus(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status);
}