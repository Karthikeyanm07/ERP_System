package com.erp.enterprise.repository.common;

import com.erp.enterprise.entity.common.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Audit Log Repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Override
    @org.springframework.lang.NonNull
    List<AuditLog> findAll();

    List<AuditLog> findByEntityNameOrderByTimestampDesc(String entityName);

    List<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Long entityId);

    List<AuditLog> findByActorUsernameOrderByTimestampDesc(String username);

    List<AuditLog> findTop100ByOrderByTimestampDesc();
}
