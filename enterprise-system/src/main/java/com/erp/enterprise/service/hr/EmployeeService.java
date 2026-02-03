package com.erp.enterprise.service.hr;

import com.erp.enterprise.dto.hr.EmployeeCreateRequest;
import com.erp.enterprise.dto.hr.EmployeeDTO;
import com.erp.enterprise.dto.hr.EmployeeListResponse;
import com.erp.enterprise.dto.hr.EmployeeDetailResponse;
import java.util.List;

// Employee Service Interface
public interface EmployeeService {

    // Create a new employee
    EmployeeDTO createEmployee(EmployeeCreateRequest request);

    // Get employee by ID
    EmployeeDTO getEmployeeById(Long id);

    // Get employee by employee code
    EmployeeDTO getEmployeeByCode(String employeeCode);

    // Get all employees (legacy - returns full DTO)
    List<EmployeeDTO> getAllEmployees();

    // Get all employees for list view (secure - excludes sensitive data)
    List<EmployeeListResponse> getAllEmployeesForList();

    // Get employee detail by ID (secure - includes all data for authorized users)
    EmployeeDetailResponse getEmployeeDetailById(Long id);

    // Get employees by department
    List<EmployeeDTO> getEmployeesByDepartment(Long departmentId);

    // Get employees by status
    List<EmployeeDTO> getEmployeesByStatus(String status);

    // Search employees
    List<EmployeeDTO> searchEmployees(String keyword);

    // Update employee
    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);

    // Delete employee
    void deleteEmployee(Long id);

    // Change employee status
    EmployeeDTO changeEmployeeStatus(Long id, String status);
}