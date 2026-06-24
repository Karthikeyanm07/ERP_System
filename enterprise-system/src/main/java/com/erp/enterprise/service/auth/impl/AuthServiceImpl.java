package com.erp.enterprise.service.auth.impl;

import com.erp.enterprise.dto.auth.*;
import com.erp.enterprise.entity.auth.PasswordResetToken;
import com.erp.enterprise.entity.hr.Role;
import com.erp.enterprise.entity.hr.User;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.repository.auth.PasswordResetTokenRepository;
import com.erp.enterprise.repository.auth.RoleRepository;
import com.erp.enterprise.repository.hr.UserRepository;
import com.erp.enterprise.security.JwtUtils;
import com.erp.enterprise.security.UserDetailsImpl;
import com.erp.enterprise.service.auth.AuthService;
import com.erp.enterprise.service.auth.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    private static final String RESET_SUCCESS_MESSAGE =
            "If an account exists with that email, you will receive a password reset link shortly.";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${erp.app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${erp.app.password-reset-expiration-minutes:60}")
    private int passwordResetExpirationMinutes;

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername() == null ? "" : loginRequest.getUsername().trim();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
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

        // Check if email exists (case-insensitive)
        String normalizedEmail = registerRequest.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException(
                    "Email is already in use",
                    "EMAIL_EXISTS"
            );
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername().trim());
        user.setEmail(registerRequest.getEmail().trim().toLowerCase());
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
    public ProfileResponse getProfile(@org.springframework.lang.NonNull Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));
        return toProfileResponse(user);
    }

    @Override
    public ProfileResponse updateProfile(@org.springframework.lang.NonNull Long userId, UpdateProfileRequest request) {
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
    public MessageResponse changePassword(@org.springframework.lang.NonNull Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect", "INVALID_PASSWORD");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.deleteByUser(user);
        return new MessageResponse("Password changed successfully");
    }

    @Override
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(user -> {
            if (Boolean.FALSE.equals(user.getIsActive())) {
                return;
            }

            passwordResetTokenRepository.deleteByUser(user);

            String rawToken = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(hashToken(rawToken));
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes));
            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl.replaceAll("/$", "") + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);
        });

        return new MessageResponse(RESET_SUCCESS_MESSAGE);
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = findValidResetToken(request.getToken());

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
        passwordResetTokenRepository.deleteByUser(user);

        return new MessageResponse("Password reset successfully. You can now sign in with your new password.");
    }

    @Override
    public MessageResponse validateResetToken(String token) {
        findValidResetToken(token);
        return new MessageResponse("Reset token is valid");
    }

    private PasswordResetToken findValidResetToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Invalid or expired reset link", "INVALID_RESET_TOKEN");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNull(hashToken(token.trim()))
                .orElseThrow(() -> new BusinessException(
                        "Invalid or expired reset link",
                        "INVALID_RESET_TOKEN"
                ));

        if (resetToken.isExpired()) {
            throw new BusinessException("Invalid or expired reset link", "INVALID_RESET_TOKEN");
        }

        return resetToken;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    @Override
    public ProfileResponse updateSessionTimeout(@org.springframework.lang.NonNull Long userId, SessionTimeoutRequest request) {
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