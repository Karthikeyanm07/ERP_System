package com.erp.enterprise.security;

import com.erp.enterprise.entity.hr.User;
import com.erp.enterprise.repository.hr.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserDetailsService Implementation
 *
 * Explanation:
 * - Spring Security uses this to load user by username
 * - Called during authentication
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        String normalizedLogin = login == null ? "" : login.trim();
        User user = userRepository.findByUsernameOrEmail(normalizedLogin)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + normalizedLogin));

        return UserDetailsImpl.build(user);
    }
}