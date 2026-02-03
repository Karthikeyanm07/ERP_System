package com.erp.enterprise.service.hr.impl;

import com.erp.enterprise.dto.hr.LeaveTypeDTO;
import com.erp.enterprise.entity.hr.LeaveType;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.hr.LeaveTypeRepository;
import com.erp.enterprise.service.hr.LeaveTypeService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Leave Type Service Implementation
 *
 * Business Logic:
 * - Manages different types of leaves (Sick, Casual, Annual, etc.)
 * - Each type has allowed days per year
 * - Prevents duplicate leave type names
 */
@Service
@Transactional
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    @Autowired
    public LeaveTypeServiceImpl(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @Override
    public LeaveTypeDTO createLeaveType(LeaveTypeDTO leaveTypeDTO) {
        // Business Logic: Check if leave type name already exists
        if (leaveTypeRepository.existsByName(leaveTypeDTO.getName())) {
            throw new DuplicateResourceException(
                    "LeaveType", "name", leaveTypeDTO.getName());
        }

        // Create leave type
        LeaveType leaveType = DtoMapper.toLeaveTypeEntity(leaveTypeDTO);
        LeaveType savedLeaveType = leaveTypeRepository.save(leaveType);

        return DtoMapper.toLeaveTypeDTO(savedLeaveType);
    }

    @Override
    public LeaveTypeDTO getLeaveTypeById(Long id) {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));

        return DtoMapper.toLeaveTypeDTO(leaveType);
    }

    @Override
    public List<LeaveTypeDTO> getAllLeaveTypes() {
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();

        return leaveTypes.stream()
                .map(DtoMapper::toLeaveTypeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveTypeDTO updateLeaveType(Long id, LeaveTypeDTO leaveTypeDTO) {
        // Find existing leave type
        LeaveType existingLeaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));

        // Business Logic: Check if new name conflicts
        if (!existingLeaveType.getName().equals(leaveTypeDTO.getName()) &&
                leaveTypeRepository.existsByName(leaveTypeDTO.getName())) {
            throw new DuplicateResourceException(
                    "LeaveType", "name", leaveTypeDTO.getName());
        }

        // Update fields
        existingLeaveType.setName(leaveTypeDTO.getName());
        existingLeaveType.setDescription(leaveTypeDTO.getDescription());
        existingLeaveType.setDaysAllowed(leaveTypeDTO.getDaysAllowed());

        LeaveType updatedLeaveType = leaveTypeRepository.save(existingLeaveType);
        return DtoMapper.toLeaveTypeDTO(updatedLeaveType);
    }

    @Override
    public void deleteLeaveType(Long id) {
        // Check if leave type exists
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));

        // In production, check if any leave requests use this type
        leaveTypeRepository.delete(leaveType);
    }
}