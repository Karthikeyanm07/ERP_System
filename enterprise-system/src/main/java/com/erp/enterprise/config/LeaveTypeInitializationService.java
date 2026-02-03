package com.erp.enterprise.config;

import com.erp.enterprise.entity.hr.LeaveType;
import com.erp.enterprise.repository.hr.LeaveTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Initialize default leave types on application startup
 * Runs after RoleInitializationService (Order 1) and AdminInitializationService (Order 2)
 */
@Component
@Order(3)
public class LeaveTypeInitializationService implements CommandLineRunner {

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveTypeInitializationService(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @Override
    public void run(String... args) {
        // Only create if no leave types exist
        if (leaveTypeRepository.count() == 0) {
            createDefaultLeaveTypes();
            System.out.println("✓ Default leave types created");
        }
    }

    private void createDefaultLeaveTypes() {
        createLeaveType("Annual Leave", "Annual/vacation leave", 20);
        createLeaveType("Sick Leave", "Medical/sick leave", 10);
        createLeaveType("Personal Leave", "Personal days", 5);
        createLeaveType("Unpaid Leave", "Leave without pay", 30);
        createLeaveType("Maternity Leave", "Maternity/paternity leave", 90);
    }

    private void createLeaveType(String name, String description, int daysAllowed) {
        LeaveType leaveType = new LeaveType();
        leaveType.setName(name);
        leaveType.setDescription(description);
        leaveType.setDaysAllowed(daysAllowed);
        leaveTypeRepository.save(leaveType);
    }
}
