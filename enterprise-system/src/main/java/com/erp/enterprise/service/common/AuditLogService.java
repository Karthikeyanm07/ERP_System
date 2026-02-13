package com.erp.enterprise.service.common;

import com.erp.enterprise.entity.common.AuditLog;
import com.erp.enterprise.entity.hr.User;
import com.erp.enterprise.repository.common.AuditLogRepository;
import com.erp.enterprise.repository.hr.UserRepository;
import com.erp.enterprise.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Audit Log Service
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository,
                           UserRepository userRepository,
                           SecurityUtils securityUtils) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    /**
     * Create an audit log entry.
     * Uses REQUIRES_NEW propagation to ensure logs are saved even if the calling transaction rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityName, Long entityId, String oldValue, String newValue) {
        Long userId = securityUtils.getCurrentUserId();
        String username = securityUtils.getCurrentUsername();

        AuditLog log = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .actorUsername(username)
                .build();

        if (userId != null) {
            userRepository.findById(userId).ifPresent(log::setActor);
        }

        auditLogRepository.save(log);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }

    public List<AuditLog> getLogsByEntity(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId);
    }
}
