package com.resourcebridge.user_service.controller;


import com.resourcebridge.user_service.entity.UserProfile;
import com.resourcebridge.user_service.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserProfileService userProfileService;

    // create profile
    @PostMapping
    public ResponseEntity<UserProfile> createProfile(
            @RequestBody UserProfile profile,
            @Req
    )
}
