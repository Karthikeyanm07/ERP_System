package com.erp.enterprise.repository.auth;

import com.erp.enterprise.entity.auth.PasswordResetToken;
import com.erp.enterprise.entity.hr.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);

    void deleteByUser(User user);
}
