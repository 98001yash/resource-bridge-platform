package com.resourcebridge.user_service.service;


import com.resourcebridge.user_service.entity.UserProfile;
import com.resourcebridge.user_service.exceptions.ResourceNotFoundException;
import com.resourcebridge.user_service.exceptions.RuntimeConflictException;
import com.resourcebridge.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserRepository userRepository;


    public UserProfile createProfile(UserProfile profile){
        log.info("Creating profile for userId ={}",profile.getUserId());

        if(userRepository.existsByUserId(profile.getUserId())){
            log.warn("Profile already exists | userId={}",profile.getUserId());

            throw new RuntimeConflictException("User already exists");
        }

        UserProfile saved = userRepository.save(profile);
        log.info("Profile created | id={} | userId ={}",
                saved.getId(),saved.getUserId());

        return saved;
    }


    public UserProfile getUserById(Long userId){
        log.info("Fetching profile | userId={}",userId);

        return userRepository.findByUserId(userId)
                .orElseThrow(()->{
                    log.warn("Profile not found | userId={}",userId);

                    return new ResourceNotFoundException(
                            "User profile not found"
                    );
                });
    }

    // update profile

    public UserProfile updateProfile(Long userId, UserProfile updated){
        log.info("Updating profile | userId={}",userId);

        UserProfile existing = getUserById(userId);
        existing.setFullName(updated.getFullName());
        existing.setPhone(updated.getPhone());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setAddress(updated.getAddress());
        existing.setCity(updated.getCity());
        existing.setState(updated.getState());
        existing.setCountry(updated.getCountry());
        existing.setOrganizationName(
                updated.getOrganizationName()
        );
        existing.setRegistrationNumber(
                updated.getRegistrationNumber()
        );

        UserProfile saved = userRepository.save(existing);
        log.info("Profile updated | userId={}", userId);
        return saved;

    }

    // ADMIN -> get by role
    public List<UserProfile> getByRole(String role){
        log.info("Fetching user by role={}",role);
        return userRepository.findByRole(role);
    }

    public List<UserProfile> getUnverified(String role){
        log.info("Fetching unverified users | role={}",role);

        return userRepository.findByRoleAndVerified(role, false);
    }


    public void deactivate(Long userId){
        log.warn("Deactivating user | userId={}",userId);

        UserProfile profile = getUserById(userId);

        profile.setActive(false);

        userRepository.save(profile);
    }
}
