package com.erp.enterprise.service.auth.impl;

import com.erp.enterprise.dto.auth.*;
import com.erp.enterprise.entity.hr.Role;
import com.erp.enterprise.entity.hr.User;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.repository.auth.RoleRepository;
import com.erp.enterprise.repository.hr.UserRepository;
import com.erp.enterprise.security.JwtUtils;
import com.erp.enterprise.security.UserDetailsImpl;
import com.erp.enterprise.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication Service Implementation
 *
 * Business Logic:
 * - Handles user registration
 * - Handles user login
 * - Generates JWT tokens
 * - Assigns roles to users
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Use user's session timeout preference if set, else default
        long expirationMs = jwtUtils.getJwtExpirationMs();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user != null && user.getSessionTimeoutMinutes() != null) {
            expirationMs = user.getSessionTimeoutMinutes() * 60L * 1000L;
        }
        String jwt = jwtUtils.generateJwtToken(authentication, expirationMs);
        long expiresInSeconds = expirationMs / 1000;

        return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                expiresInSeconds
        );
    }

    @Override
    public MessageResponse register(RegisterRequest registerRequest) {
        /**
         * Business Logic:
         * 1. Check if username or email already exists
         * 2. Create new user with encrypted password
         * 3. Assign roles (default to USER if none specified)
         * 4. Save user to database
         */

        // Check if username exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BusinessException(
                    "Username is already taken",
                    "USERNAME_EXISTS"
            );
        }

        // Check if email exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BusinessException(
                    "Email is already in use",
                    "EMAIL_EXISTS"
            );
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setIsActive(true);

        // Assign roles - PUBLIC registration only allows ROLE_USER
        // Admin must manually assign elevated roles
        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        // Always assign USER role for public registration
        // Elevated roles (HR, ADMIN, etc.) must be assigned by admin
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new BusinessException(
                        "Role ROLE_USER not found. Please initialize roles first.",
                        "ROLE_NOT_FOUND"
                ));
        roles.add(userRole);

        // Log if user tried to request elevated roles
        if (strRoles != null && !strRoles.isEmpty()) {
            strRoles.stream()
                    .filter(role -> !role.equals("ROLE_USER"))
                    .forEach(role -> 
                        org.slf4j.LoggerFactory.getLogger(AuthServiceImpl.class)
                            .warn("Blocked attempt to self-assign role: {}", role)
                    );
        }

        user.setRoles(roles);

        // Save user
        userRepository.save(user);

        return new MessageResponse("User registered successfully!");
    }

    @Override
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));
        return toProfileResponse(user);
    }

    @Override
    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username is already taken", "USERNAME_EXISTS");
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already in use", "EMAIL_EXISTS");
        }

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        userRepository.save(user);
        return toProfileResponse(user);
    }

    @Override
    public MessageResponse changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect", "INVALID_PASSWORD");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return new MessageResponse("Password changed successfully");
    }

    @Override
    public ProfileResponse updateSessionTimeout(Long userId, SessionTimeoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));
        user.setSessionTimeoutMinutes(request.getSessionTimeoutMinutes());
        userRepository.save(user);
        return toProfileResponse(user);
    }

    private ProfileResponse toProfileResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles,
                user.getSessionTimeoutMinutes()
        );
    }
}