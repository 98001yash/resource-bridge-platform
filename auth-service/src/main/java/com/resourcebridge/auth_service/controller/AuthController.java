package com.resourcebridge.auth_service.controller;


import com.resourcebridge.auth_service.advices.ApiResponse;
import com.resourcebridge.auth_service.dtos.*;
import com.resourcebridge.auth_service.dtos.UserResponseDto;

import com.resourcebridge.auth_service.repository.UserRepository;
import com.resourcebridge.auth_service.service.AuthService;
import com.resourcebridge.auth_service.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;


    // signup

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody SignupRequestDto dto){

        log.info("API call-> signup | email={}",dto.getEmail());
        UserResponseDto response = authService.signup(dto);
        log.info("Signup successful | userId={}",response.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // login
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto dto) {

        log.info("API Call → Login | email={}", dto.getEmail());
        String token = authService.login(dto);
        log.info("Login success | email={}", dto.getEmail());

        AuthResponseDto response =
                new AuthResponseDto(token, "Bearer");

        return ResponseEntity.ok(response);
    }

    // current user
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        log.debug("API Call → Get Current User");

        UserResponseDto user =
                authService.getCurrentUser(authHeader);

        return ResponseEntity.ok(user);
    }


    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {

        return ResponseEntity.ok(
                ApiResponse.success("Auth Service is UP 🚀")
        );
    }


    @PutMapping("/admin/verify/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> verifyUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {

        // Extract token
        String token = authHeader.substring(7);

        // Get adminId from JWT
        Long adminId = jwtService.extractUserId(token);

        log.info("Admin verification | adminId={} | targetUserId={}",
                adminId, userId);

        authService.verifyUser(userId, adminId);

        return ResponseEntity.ok(
                ApiResponse.success("User verified successfully")
        );
    }


}
