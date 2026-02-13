package com.erp.enterprise.service.hr;

import com.erp.enterprise.dto.hr.DepartmentDTO;
import java.util.List;

// Department Service Interface
// Business Logic: Defines contract for department operations
public interface DepartmentService {

    // Create a new department
    @org.springframework.lang.NonNull DepartmentDTO createDepartment(@org.springframework.lang.NonNull DepartmentDTO departmentDTO);

    // Get department by ID
    @org.springframework.lang.NonNull DepartmentDTO getDepartmentById(@org.springframework.lang.NonNull Long id);

    // Get all departments
    List<DepartmentDTO> getAllDepartments();

    // Update department
    @org.springframework.lang.NonNull DepartmentDTO updateDepartment(@org.springframework.lang.NonNull Long id, @org.springframework.lang.NonNull DepartmentDTO departmentDTO);

    // Delete department
    void deleteDepartment(@org.springframework.lang.NonNull Long id);

    // Assign manager to department
    @org.springframework.lang.NonNull DepartmentDTO assignManager(@org.springframework.lang.NonNull Long departmentId, @org.springframework.lang.NonNull Long managerId);
}