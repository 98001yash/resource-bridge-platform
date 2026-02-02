package com.resourcebridge.user_service.repository;

import com.resourcebridge.user_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserProfile,Long> {


    Optional<UserProfile> findByUserId(Long userId);
    boolean existsById(Long userId);

    Optional<UserProfile> findByEmail(String email);


    List<UserProfile> findByRole(String role);

    List<UserProfile> findByVerified(boolean verified);

    List<UserProfile> findByActive(boolean active);

    // admin
    List<UserProfile> findByRoleAndVerified(String role, boolean verified);
}
