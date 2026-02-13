package com.erp.enterprise.service.hr.impl;

import com.erp.enterprise.dto.hr.DepartmentDTO;
import com.erp.enterprise.entity.hr.Department;
import com.erp.enterprise.entity.hr.Employee;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.hr.DepartmentRepository;
import com.erp.enterprise.repository.hr.EmployeeRepository;
import com.erp.enterprise.service.hr.DepartmentService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// Department Service Implementation
// Business Logic: All department-related business operations
@Service
@Transactional  // All methods run in a database transaction
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    // Constructor injection (recommended over @Autowired on fields)
    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @org.springframework.lang.NonNull
    public DepartmentDTO createDepartment(@org.springframework.lang.NonNull DepartmentDTO departmentDTO) {
        // Business Logic: Check if department name already exists
        if (departmentRepository.existsByName(departmentDTO.getName())) {
            throw new DuplicateResourceException("Department", "name", departmentDTO.getName());
        }

        // Convert DTO to Entity
        Department department = DtoMapper.toDepartmentEntity(departmentDTO);

        // If manager ID is provided, set the manager
        Long managerId = departmentDTO.getManagerId();
        if (managerId != null) {
            Employee manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee", "id", managerId));
            department.setManager(manager);
        }

        // Save to database
        Department savedDepartment = departmentRepository.save(department);

        // Convert back to DTO and return
        return DtoMapper.toDepartmentDTO(savedDepartment);
    }

    @Override
    @org.springframework.lang.NonNull
    public DepartmentDTO getDepartmentById(@org.springframework.lang.NonNull Long id) {
        // Business Logic: Find department or throw exception
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        return DtoMapper.toDepartmentDTO(department);
    }

    @Override
    public List<DepartmentDTO> getAllDepartments() {
        // Get all departments from database
        List<Department> departments = departmentRepository.findAll();

        // Convert each entity to DTO using streams
        return departments.stream()
                .map(DtoMapper::toDepartmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @org.springframework.lang.NonNull
    public DepartmentDTO updateDepartment(@org.springframework.lang.NonNull Long id, @org.springframework.lang.NonNull DepartmentDTO departmentDTO) {
        // Business Logic: Check if department exists
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Business Logic: Check if new name conflicts with another department
        if (!existingDepartment.getName().equals(departmentDTO.getName()) &&
                departmentRepository.existsByName(departmentDTO.getName())) {
            throw new DuplicateResourceException("Department", "name", departmentDTO.getName());
        }

        // Update fields
        existingDepartment.setName(departmentDTO.getName());
        existingDepartment.setDescription(departmentDTO.getDescription());

        // Update manager if provided
        Long managerId = departmentDTO.getManagerId();
        if (managerId != null) {
            Employee manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee", "id", managerId));
            existingDepartment.setManager(manager);
        } else {
            existingDepartment.setManager(null);  // Remove manager
        }

        // Save updated department
        Department updatedDepartment = departmentRepository.save(existingDepartment);

        return DtoMapper.toDepartmentDTO(updatedDepartment);
    }

    @Override
    public void deleteDepartment(@org.springframework.lang.NonNull Long id) {
        // Business Logic: Check if department exists
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Business Logic: Check if department has employees
        long employeeCount = employeeRepository.countByDepartmentId(id);
        if (employeeCount > 0) {
            throw new BusinessException(
                    "Cannot delete department with " + employeeCount + " employees. " +
                            "Please reassign or remove employees first.",
                    "DEPARTMENT_HAS_EMPLOYEES"
            );
        }

        // Delete department
        departmentRepository.delete(department);
    }

    @Override
    @org.springframework.lang.NonNull
    public DepartmentDTO assignManager(@org.springframework.lang.NonNull Long departmentId, @org.springframework.lang.NonNull Long managerId) {
        // Business Logic: Find department
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        // Business Logic: Find manager
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", managerId));

        // Assign manager
        department.setManager(manager);

        // Save and return
        Department updatedDepartment = departmentRepository.save(department);
        return DtoMapper.toDepartmentDTO(updatedDepartment);
    }
}