package com.erp.enterprise.repository.hr;

import com.erp.enterprise.entity.hr.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Leave Type Repository
 */
@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    @Override
    @org.springframework.lang.NonNull
    List<LeaveType> findAll();

    // Check if leave type name exists
    boolean existsByName(String name);

    // Find leave type by name
    Optional<LeaveType> findByName(String name);
}