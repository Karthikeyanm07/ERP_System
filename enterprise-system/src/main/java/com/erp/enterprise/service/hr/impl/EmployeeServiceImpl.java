package com.erp.enterprise.service.hr.impl;

import com.erp.enterprise.dto.hr.EmployeeCreateRequest;
import com.erp.enterprise.dto.hr.EmployeeDTO;
import com.erp.enterprise.dto.hr.EmployeeListResponse;
import com.erp.enterprise.dto.hr.EmployeeDetailResponse;
import com.erp.enterprise.entity.hr.Department;
import com.erp.enterprise.entity.hr.Employee;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.hr.DepartmentRepository;
import com.erp.enterprise.repository.hr.EmployeeRepository;
import com.erp.enterprise.service.hr.EmployeeService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// Employee Service Implementation
// Business Logic: All employee-related business operations
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public EmployeeDTO createEmployee(EmployeeCreateRequest request) {
        // Business Logic: Check if employee code already exists
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateResourceException("Employee", "employeeCode", request.getEmployeeCode());
        }

        // Business Logic: Check if email already exists
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }

        // Create employee entity
        Employee employee = new Employee();
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDesignation(request.getDesignation());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setSalary(request.getSalary());
        employee.setStatus("ACTIVE");  // New employees are active by default

        // Business Logic: Set department if provided
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getDepartmentId()));
            employee.setDepartment(department);
        }

        // Save employee
        Employee savedEmployee = employeeRepository.save(employee);

        return DtoMapper.toEmployeeDTO(savedEmployee);
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        return DtoMapper.toEmployeeDTO(employee);
    }

    @Override
    public EmployeeDTO getEmployeeByCode(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee", "employeeCode", employeeCode));

        return DtoMapper.toEmployeeDTO(employee);
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
                .map(DtoMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all employees for list view (secure - excludes sensitive data)
     * Business Logic: Returns lightweight DTO without salary/phone
     * Security: Safe to expose in Network tab
     */
    @Override
    public List<EmployeeListResponse> getAllEmployeesForList() {
        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
                .map(DtoMapper::toEmployeeListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get employee detail by ID (secure - includes all data)
     * Business Logic: Returns full employee details
     * Security: Should be access-controlled (HR/Admin only)
     */
    @Override
    public EmployeeDetailResponse getEmployeeDetailById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        return DtoMapper.toEmployeeDetailResponse(employee);
    }

    @Override
    public List<EmployeeDTO> getEmployeesByDepartment(Long departmentId) {
        // Business Logic: Verify department exists
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }

        List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);

        return employees.stream()
                .map(DtoMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDTO> getEmployeesByStatus(String status) {
        if (!isValidStatus(status)) {
            throw new BusinessException("Invalid employee status: " + status, "INVALID_STATUS");
        }
        List<Employee> employees = employeeRepository.findByStatus(status);

        return employees.stream()
                .map(DtoMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDTO> searchEmployees(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees();
        }

        List<Employee> employees = employeeRepository.searchEmployees(keyword.trim());

        return employees.stream()
                .map(DtoMapper::toEmployeeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        // Business Logic: Find existing employee
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Business Logic: Check if new employee code conflicts
        if (!existingEmployee.getEmployeeCode().equals(employeeDTO.getEmployeeCode()) &&
                employeeRepository.existsByEmployeeCode(employeeDTO.getEmployeeCode())) {
            throw new DuplicateResourceException("Employee", "employeeCode", employeeDTO.getEmployeeCode());
        }

        // Business Logic: Check if new email conflicts
        if (!existingEmployee.getEmail().equals(employeeDTO.getEmail()) &&
                employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", employeeDTO.getEmail());
        }

        // Update fields
        existingEmployee.setEmployeeCode(employeeDTO.getEmployeeCode());
        existingEmployee.setFirstName(employeeDTO.getFirstName());
        existingEmployee.setLastName(employeeDTO.getLastName());
        existingEmployee.setEmail(employeeDTO.getEmail());
        existingEmployee.setPhone(employeeDTO.getPhone());
        existingEmployee.setDesignation(employeeDTO.getDesignation());
        existingEmployee.setDateOfJoining(employeeDTO.getDateOfJoining());
        existingEmployee.setSalary(employeeDTO.getSalary());

        // Update department if provided
        if (employeeDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", employeeDTO.getDepartmentId()));
            existingEmployee.setDepartment(department);
        } else {
            existingEmployee.setDepartment(null);
        }

        // Save updated employee
        Employee updatedEmployee = employeeRepository.save(existingEmployee);

        return DtoMapper.toEmployeeDTO(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        // Business Logic: Soft delete to preserve referential integrity
        // Cannot hard delete employees with attendance, leave requests, etc.
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Check if employee has related records (attendance, leaves, etc.)
        // Instead of hard delete, mark as TERMINATED (soft delete)
        if (employee.getStatus().equals("TERMINATED")) {
            throw new BusinessException(
                "Employee is already terminated. Cannot delete terminated employees.",
                "ALREADY_TERMINATED"
            );
        }

        // Soft delete: Change status to TERMINATED
        employee.setStatus("TERMINATED");
        employeeRepository.save(employee);
        
        // Note: If you need to hard delete, you must first delete all related records:
        // - Attendance records
        // - Leave requests
        // - Payroll records
        // - Any other references
    }

    @Override
    public EmployeeDTO changeEmployeeStatus(Long id, String status) {
        // Business Logic: Validate status
        if (!isValidStatus(status)) {
            throw new BusinessException("Invalid employee status: " + status, "INVALID_STATUS");
        }

        // Find employee
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Change status
        employee.setStatus(status);

        // Save and return
        Employee updatedEmployee = employeeRepository.save(employee);
        return DtoMapper.toEmployeeDTO(updatedEmployee);
    }

    // Helper method to validate status
    private boolean isValidStatus(String status) {
        return status != null &&
                (status.equals("ACTIVE") || status.equals("INACTIVE") || status.equals("TERMINATED"));
    }
}