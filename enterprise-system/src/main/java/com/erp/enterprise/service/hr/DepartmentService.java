package com.erp.enterprise.service.hr;

import com.erp.enterprise.dto.hr.DepartmentDTO;
import java.util.List;

// Department Service Interface
// Business Logic: Defines contract for department operations
public interface DepartmentService {

    // Create a new department
    DepartmentDTO createDepartment(DepartmentDTO departmentDTO);

    // Get department by ID
    DepartmentDTO getDepartmentById(Long id);

    // Get all departments
    List<DepartmentDTO> getAllDepartments();

    // Update department
    DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO);

    // Delete department
    void deleteDepartment(Long id);

    // Assign manager to department
    DepartmentDTO assignManager(Long departmentId, Long managerId);
}