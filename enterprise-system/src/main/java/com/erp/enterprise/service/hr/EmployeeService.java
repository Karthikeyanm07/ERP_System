package com.erp.enterprise.service.hr;

import com.erp.enterprise.dto.hr.EmployeeCreateRequest;
import com.erp.enterprise.dto.hr.EmployeeDTO;
import com.erp.enterprise.dto.hr.EmployeeListResponse;
import com.erp.enterprise.dto.hr.EmployeeDetailResponse;
import java.util.List;

// Employee Service Interface
public interface EmployeeService {

    // Create a new employee
    @org.springframework.lang.NonNull EmployeeDTO createEmployee(@org.springframework.lang.NonNull EmployeeCreateRequest request);

    // Get employee by ID
    @org.springframework.lang.NonNull EmployeeDTO getEmployeeById(@org.springframework.lang.NonNull Long id);

    // Get employee by employee code
    @org.springframework.lang.NonNull EmployeeDTO getEmployeeByCode(@org.springframework.lang.NonNull String employeeCode);

    // Get all employees (legacy - returns full DTO)
    List<EmployeeDTO> getAllEmployees();

    // Get all employees for list view (secure - excludes sensitive data)
    List<EmployeeListResponse> getAllEmployeesForList();

    // Get employee detail by ID (secure - includes all data for authorized users)
    @org.springframework.lang.NonNull EmployeeDetailResponse getEmployeeDetailById(@org.springframework.lang.NonNull Long id);

    // Get employees by department
    List<EmployeeDTO> getEmployeesByDepartment(@org.springframework.lang.NonNull Long departmentId);

    // Get employees by status
    List<EmployeeDTO> getEmployeesByStatus(@org.springframework.lang.NonNull String status);

    // Search employees
    List<EmployeeDTO> searchEmployees(String keyword);

    // Update employee
    @org.springframework.lang.NonNull EmployeeDTO updateEmployee(@org.springframework.lang.NonNull Long id, @org.springframework.lang.NonNull EmployeeDTO employeeDTO);

    // Delete employee
    void deleteEmployee(@org.springframework.lang.NonNull Long id);

    // Change employee status
    @org.springframework.lang.NonNull EmployeeDTO changeEmployeeStatus(@org.springframework.lang.NonNull Long id, @org.springframework.lang.NonNull String status);
}