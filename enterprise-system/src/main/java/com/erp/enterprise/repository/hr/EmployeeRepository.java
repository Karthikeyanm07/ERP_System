package com.erp.enterprise.repository.hr;

import com.erp.enterprise.entity.hr.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository for Employee
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Override
    @EntityGraph(attributePaths = {"department"})
    @org.springframework.lang.NonNull
    List<Employee> findAll();

    // Check if employee code exists (for duplicate prevention)
    boolean existsByEmployeeCode(String employeeCode);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find employee by employee code
    Optional<Employee> findByEmployeeCode(String employeeCode);

    // Find employee by email
    Optional<Employee> findByEmail(String email);

    // Find all employees in a department
    // Business Logic: Useful for department-wise reports
    List<Employee> findByDepartmentId(Long departmentId);

    // Find employees by status (ACTIVE, INACTIVE, TERMINATED)
    List<Employee> findByStatus(String status);

    // Custom JPQL query to search employees by name
    // Business Logic: Search functionality for frontend
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Employee> searchEmployees(@Param("keyword") String keyword);

    // Count employees in a department
    long countByDepartmentId(Long departmentId);
}