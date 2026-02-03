package com.erp.enterprise.repository.auth;

import com.erp.enterprise.entity.hr.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Role Repository
 *
 * Explanation:
 * - Manages role data
 * - Used for assigning roles to users
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}