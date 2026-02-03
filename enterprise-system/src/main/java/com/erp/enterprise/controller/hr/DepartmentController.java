package com.erp.enterprise.controller.hr;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.hr.DepartmentDTO;
import com.erp.enterprise.service.hr.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Department operations
 *
 * Base URL: /api/departments
 *
 * Available endpoints:
 * - GET    /api/departments              -> Get all departments
 * - GET    /api/departments/{id}         -> Get department by ID
 * - POST   /api/departments              -> Create new department
 * - PUT    /api/departments/{id}         -> Update department
 * - DELETE /api/departments/{id}         -> Delete department
 * - PUT    /api/departments/{id}/manager/{managerId} -> Assign manager
 * 
 * Security:
 * - All endpoints require ROLE_ADMIN or ROLE_HR
 */
@RestController
@RequestMapping("/api/departments")
@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * GET /api/departments
     * Get all departments
     *
     * Response: 200 OK with list of departments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(
                ApiResponse.success("Departments retrieved successfully", departments)
        );
    }

    /**
     * GET /api/departments/{id}
     * Get department by ID
     *
     * @param id - Department ID
     * Response: 200 OK if found, 404 Not Found if doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentById(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Department retrieved successfully", department)
        );
    }

    /**
     * POST /api/departments
     * Create new department
     *
     * @param departmentDTO - Department data from request body
     * @Valid annotation triggers validation on the DTO
     * Response: 201 Created with created department
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(
            @Valid @RequestBody DepartmentDTO departmentDTO) {

        DepartmentDTO createdDepartment = departmentService.createDepartment(departmentDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", createdDepartment));
    }

    /**
     * PUT /api/departments/{id}
     * Update existing department
     *
     * @param id - Department ID to update
     * @param departmentDTO - Updated department data
     * Response: 200 OK with updated department
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentDTO departmentDTO) {

        DepartmentDTO updatedDepartment = departmentService.updateDepartment(id, departmentDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Department updated successfully", updatedDepartment)
        );
    }

    /**
     * DELETE /api/departments/{id}
     * Delete department
     *
     * Business Logic: Cannot delete if department has employees
     *
     * @param id - Department ID to delete
     * Response: 200 OK if deleted, 400 Bad Request if has employees
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(
                ApiResponse.success("Department deleted successfully", null)
        );
    }

    /**
     * PUT /api/departments/{id}/manager/{managerId}
     * Assign manager to department
     *
     * @param id - Department ID
     * @param managerId - Employee ID to set as manager
     * Response: 200 OK with updated department
     */
    @PutMapping("/{id}/manager/{managerId}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> assignManager(
            @PathVariable Long id,
            @PathVariable Long managerId) {

        DepartmentDTO department = departmentService.assignManager(id, managerId);
        return ResponseEntity.ok(
                ApiResponse.success("Manager assigned successfully", department)
        );
    }
}