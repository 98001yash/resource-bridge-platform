package com.resourcebridge.auth_service.service;

import com.resourcebridge.auth_service.dtos.LoginRequestDto;
import com.resourcebridge.auth_service.dtos.SignupRequestDto;
import com.resourcebridge.auth_service.dtos.UserResponseDto;
import com.resourcebridge.auth_service.entity.User;
import com.resourcebridge.auth_service.enums.Role;
import com.resourcebridge.auth_service.exceptions.ResourceNotFoundException;
import com.resourcebridge.auth_service.exceptions.RuntimeConflictException;
import com.resourcebridge.auth_service.kafka.AdminEventProducer;
import com.resourcebridge.auth_service.kafka.UserEventProducer;
import com.resourcebridge.auth_service.repository.UserRepository;
import com.resourcebridge.auth_service.utils.PasswordUtils;


import com.resourcebridge.events.AdminActionType;
import com.resourcebridge.events.AdminVerificationEvent;
import com.resourcebridge.events.EventType;
import com.resourcebridge.events.UserEvent;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.locationtech.jts.geom.Point;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final GeoCodingService geoCodingService;

    // Kafka Producers
    private final UserEventProducer userEventProducer;
    private final AdminEventProducer adminEventProducer;

    /* ================= SIGNUP ================= */

    public UserResponseDto signup(SignupRequestDto dto) {

        log.info("Signup request | email={} | role={}",
                dto.getEmail(), dto.getRole());

        try {

            if (userRepository.existsByEmail(dto.getEmail())) {
                log.warn("Signup blocked | email already exists: {}",
                        dto.getEmail());
                throw new RuntimeConflictException("Email already registered");
            }

            // Geocoding
            log.debug("Geocoding address | email={}", dto.getEmail());

            Point location = geoCodingService.getLocation(dto.getAddress());

            // Map DTO → Entity
            User user = modelMapper.map(dto, User.class);

            user.setPassword(
                    PasswordUtils.hashPassword(dto.getPassword())
            );

            user.setRole(dto.getRole());
            user.setLocation(location);
            user.setEnabled(true);
            user.setLocked(false);

            // Verification logic
            if (dto.getRole() == Role.NGO || dto.getRole() == Role.CLINIC) {

                user.setVerified(false);

                log.info("Verification pending | email={} | role={}",
                        dto.getEmail(), dto.getRole());

            } else {
                user.setVerified(true);
            }

            // Save user
            User saved = userRepository.save(user);

            log.info("User registered | id={} | role={}",
                    saved.getId(), saved.getRole());

            /* ================= PRODUCE USER_CREATED EVENT ================= */

            UserEvent userCreatedEvent = UserEvent.builder()
                    .source("auth-service")
                    .eventType(EventType.USER_CREATED)
                    .userId(saved.getId())
                    .email(saved.getEmail())
                    .role(saved.getRole().name())
                    .verified(saved.isVerified())
                    .enabled(saved.isEnabled())
                    .build();

            userEventProducer.sendUserEvent(userCreatedEvent);

            log.info("USER_CREATED event published | userId={}",
                    saved.getId());

            return modelMapper.map(saved, UserResponseDto.class);

        } catch (Exception e) {

            log.error("Signup failed | email={} | reason={}",
                    dto.getEmail(), e.getMessage(), e);

            throw e;
        }
    }

    /* ================= LOGIN ================= */

    public String login(LoginRequestDto dto) {

        log.info("Login attempt | email={}", dto.getEmail());

        try {

            User user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> {
                        log.warn("Login failed | user not found | email={}",
                                dto.getEmail());
                        return new ResourceNotFoundException("Invalid credentials");
                    });

            if (!PasswordUtils.checkPassword(
                    dto.getPassword(), user.getPassword())) {

                log.warn("Login failed | wrong password | email={}",
                        dto.getEmail());

                throw new BadCredentialsException("Invalid credentials");
            }

            if (!user.isEnabled() || user.isLocked()) {

                log.warn("Login blocked | account disabled | userId={}",
                        user.getId());

                throw new RuntimeException("Account disabled");
            }

            String token = jwtService.generateAccessToken(user);

            log.info("Login success | userId={} | role={}",
                    user.getId(), user.getRole());

            return token;

        } catch (Exception e) {

            log.error("Login failed | email={} | reason={}",
                    dto.getEmail(), e.getMessage());

            throw e;
        }
    }

    /* ================= CURRENT USER ================= */

    public UserResponseDto getCurrentUser(String authHeader) {

        log.debug("Fetching current user from token");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            log.warn("Invalid Authorization header");

            throw new BadCredentialsException("Invalid token");
        }

        try {

            String token = authHeader.substring(7);

            Claims claims = jwtService.extractAllClaims(token);

            Long userId = claims.get("userId", Long.class);

            log.debug("Token validated | userId={}", userId);

            return getUserById(userId);

        } catch (Exception e) {

            log.error("JWT parsing failed | reason={}", e.getMessage());

            throw e;
        }
    }

    /* ================= GET BY ID ================= */

    public UserResponseDto getUserById(Long id) {

        log.debug("Fetching user | id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn("User not found | id={}", id);

                    return new ResourceNotFoundException("User not found");
                });

        log.debug("User loaded | id={} | role={}",
                user.getId(), user.getRole());

        return modelMapper.map(user, UserResponseDto.class);
    }

    /* ================= VERIFY USER (ADMIN) ================= */

    public void verifyUser(Long userId, Long adminId) {

        log.info("Verification request | userId={} | adminId={}",
                userId, adminId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.isVerified()) {

            log.warn("User already verified | userId={}", userId);
            return;
        }

        user.setVerified(true);

        userRepository.save(user);

        log.info("User verified successfully | userId={}", userId);

        /* ================= PRODUCE USER_VERIFIED EVENT ================= */

        UserEvent verifiedEvent = UserEvent.builder()
                .source("auth-service")
                .eventType(EventType.USER_VERIFIED)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .verified(true)
                .enabled(user.isEnabled())
                .build();

        userEventProducer.sendUserEvent(verifiedEvent);

        log.info("USER_VERIFIED event published | userId={}", userId);

        /* ================= PRODUCE ADMIN_VERIFICATION EVENT ================= */

        AdminVerificationEvent adminEvent =
                AdminVerificationEvent.builder()
                        .source("auth-service")
                        .eventType(EventType.ADMIN_VERIFICATION)
                        .userId(user.getId())
                        .adminId(adminId.toString())
                        .action(AdminActionType.VERIFIED)
                        .remarks("Approved by admin")
                        .verified(true)
                        .build();

        adminEventProducer.sendAdminVerificationEvent(adminEvent);

        log.info("ADMIN_VERIFICATION event published | userId={}", userId);
    }

}
