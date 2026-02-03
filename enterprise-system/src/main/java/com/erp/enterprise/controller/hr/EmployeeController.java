package com.erp.enterprise.controller.hr;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.hr.EmployeeCreateRequest;
import com.erp.enterprise.dto.hr.EmployeeDTO;
import com.erp.enterprise.dto.hr.EmployeeListResponse;
import com.erp.enterprise.dto.hr.EmployeeDetailResponse;
import com.erp.enterprise.service.hr.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Employee operations
 *
 * Base URL: /api/employees
 *
 * Available endpoints:
 * - GET    /api/employees                      -> Get all employees
 * - GET    /api/employees/{id}                 -> Get employee by ID
 * - GET    /api/employees/code/{code}          -> Get employee by code
 * - GET    /api/employees/department/{deptId}  -> Get employees by department
 * - GET    /api/employees/status/{status}      -> Get employees by status
 * - GET    /api/employees/search?keyword=xyz   -> Search employees
 * - POST   /api/employees                      -> Create new employee
 * - PUT    /api/employees/{id}                 -> Update employee
 * - PUT    /api/employees/{id}/status/{status} -> Change employee status --
 * - DELETE /api/employees/{id}                 -> Delete employee
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * GET /api/employees
     * Get all employees (secure - excludes sensitive data)
     *
     * Security: Returns EmployeeListResponse without salary/phone
     * Response: 200 OK with list of employees (public fields only)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeListResponse>>> getAllEmployees() {
        List<EmployeeListResponse> employees = employeeService.getAllEmployeesForList();
        return ResponseEntity.ok(
                ApiResponse.success("Employees retrieved successfully", employees)
        );
    }

    /**
     * GET /api/employees/{id}
     * Get employee by ID (secure - includes all data for authorized users)
     *
     * Security: Returns EmployeeDetailResponse with sensitive data
     * Access Control: Should be restricted to HR/Admin or employee themselves
     *
     * @param id - Employee ID
     * Response: 200 OK if found, 404 Not Found if doesn't exist
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDetailResponse>> getEmployeeById(@PathVariable Long id) {
        EmployeeDetailResponse employee = employeeService.getEmployeeDetailById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Employee retrieved successfully", employee)
        );
    }

    /**
     * GET /api/employees/code/{code}
     * Get employee by employee code
     *
     * Example: /api/employees/code/EMP001
     *
     * @param code - Employee code
     * Response: 200 OK if found, 404 Not Found if doesn't exist
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeByCode(@PathVariable String code) {
        EmployeeDTO employee = employeeService.getEmployeeByCode(code);
        return ResponseEntity.ok(
                ApiResponse.success("Employee retrieved successfully", employee)
        );
    }

    /**
     * GET /api/employees/department/{departmentId}
     * Get all employees in a specific department
     *
     * Business Logic: Useful for department-wise reports
     *
     * @param departmentId - Department ID
     * Response: 200 OK with list of employees
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployeesByDepartment(
            @PathVariable Long departmentId) {

        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(
                ApiResponse.success("Employees retrieved successfully", employees)
        );
    }

    /**
     * GET /api/employees/status/{status}
     * Get employees by status
     *
     * Valid statuses: ACTIVE, INACTIVE, TERMINATED
     * Example: /api/employees/status/ACTIVE
     *
     * @param status - Employee status
     * Response: 200 OK with list of employees
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployeesByStatus(
            @PathVariable String status) {

        List<EmployeeDTO> employees = employeeService.getEmployeesByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success("Employees retrieved successfully", employees)
        );
    }

    /**
     * GET /api/employees/search?keyword=john
     * Search employees by keyword
     *
     * Searches in: firstName, lastName, email, employeeCode
     * Example: /api/employees/search?keyword=john
     *
     * @param keyword - Search keyword (query parameter)
     * Response: 200 OK with matching employees
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> searchEmployees(
            @RequestParam(required = false) String keyword) {

        List<EmployeeDTO> employees = employeeService.searchEmployees(keyword);
        return ResponseEntity.ok(
                ApiResponse.success("Search completed successfully", employees)
        );
    }

    /**
     * POST /api/employees
     * Create new employee
     *
     * Business Logic: Validates uniqueness of employee code and email
     *
     * @param request - Employee creation request data
     * Response: 201 Created with created employee
     */
    @PostMapping
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request) {

        EmployeeDTO createdEmployee = employeeService.createEmployee(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", createdEmployee));
    }

    /**
     * PUT /api/employees/{id}
     * Update existing employee
     *
     * Business Logic: Validates uniqueness if code/email changed
     *
     * @param id - Employee ID to update
     * @param employeeDTO - Updated employee data
     * Response: 200 OK with updated employee
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO) {

        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Employee updated successfully", updatedEmployee)
        );
    }

    /**
     * PUT /api/employees/{id}/status/{status}
     * Change employee status
     *
     * Valid statuses: ACTIVE, INACTIVE, TERMINATED
     * Example: /api/employees/1/status/INACTIVE
     *
     * @param id - Employee ID
     * @param status - New status
     * Response: 200 OK with updated employee
     */
    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> changeEmployeeStatus(
            @PathVariable Long id,
            @PathVariable String status) {

        EmployeeDTO employee = employeeService.changeEmployeeStatus(id, status);
        return ResponseEntity.ok(
                ApiResponse.success("Employee status updated successfully", employee)
        );
    }

    /**
     * DELETE /api/employees/{id}
     * Delete employee
     *
     * Business Logic: In production, might want soft delete instead
     *
     * @param id - Employee ID to delete
     * Response: 200 OK if deleted
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(
                ApiResponse.success("Employee deleted successfully", null)
        );
    }
}