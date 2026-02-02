package com.resourcebridge.user_service.controller;


import com.resourcebridge.user_service.auth.RoleAllowed;
import com.resourcebridge.user_service.auth.UserContextHolder;
import com.resourcebridge.user_service.entity.UserProfile;
import com.resourcebridge.user_service.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    @RoleAllowed({"DONOR", "NGO", "CLINIC", "VOLUNTEER", "ADMIN"})
    public ResponseEntity<UserProfile> getMyProfile() {

        Long userId =
                UserContextHolder.getCurrentUserId();

        log.info("Get profile | userId={}", userId);
        UserProfile profile =
                userProfileService.getUserById(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    @RoleAllowed({"DONOR", "NGO", "CLINIC", "VOLUNTEER", "ADMIN"})
    public ResponseEntity<UserProfile> updateMyProfile(
            @RequestBody UserProfile updatedProfile) {

        Long userId =
                UserContextHolder.getCurrentUserId();

        log.info("Update profile | userId={}", userId);

        UserProfile updated =
                userProfileService.updateProfile(
                        userId,
                        updatedProfile
                );

        return ResponseEntity.ok(updated);
    }


    @PostMapping
    @RoleAllowed({"ADMIN"})
    public ResponseEntity<UserProfile> createProfile(
            @RequestBody UserProfile profile) {

        Long creatorId =
                UserContextHolder.getCurrentUserId();

        log.info("Create profile | by admin={}",
                creatorId);

        UserProfile saved =
                userProfileService.createProfile(profile);

        return ResponseEntity.ok(saved);
    }


    @GetMapping("/admin/role/{role}")
    @RoleAllowed({"ADMIN"})
    public ResponseEntity<List<UserProfile>> getByRole(
            @PathVariable String role) {

        Long adminId =
                UserContextHolder.getCurrentUserId();

        log.info("Admin {} fetch by role={}",
                adminId, role);

        List<UserProfile> users =
                userProfileService.getByRole(role);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/unverified/{role}")
    @RoleAllowed({"ADMIN"})
    public ResponseEntity<List<UserProfile>> getUnverified(
            @PathVariable String role) {

        Long adminId =
                UserContextHolder.getCurrentUserId();

        log.info("Admin {} fetch unverified role={}",
                adminId, role);

        List<UserProfile> users =
                userProfileService.getUnverified(role);

        return ResponseEntity.ok(users);
    }

    @PutMapping("/admin/deactivate/{userId}")
    @RoleAllowed({"ADMIN"})
    public ResponseEntity<String> deactivate(
            @PathVariable Long userId) {

        Long adminId =
                UserContextHolder.getCurrentUserId();

        log.warn("Admin {} deactivating user {}",
                adminId, userId);

        userProfileService.deactivate(userId);

        return ResponseEntity.ok("User deactivated");
    }

}
