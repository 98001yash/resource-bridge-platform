package com.resourcebridge.auth_service.service;


import com.resourcebridge.auth_service.dtos.LoginRequestDto;
import com.resourcebridge.auth_service.dtos.SignupRequestDto;
import com.resourcebridge.auth_service.dtos.UserResponseDto;
import com.resourcebridge.auth_service.entity.User;
import com.resourcebridge.auth_service.enums.Role;
import com.resourcebridge.auth_service.exceptions.ResourceNotFoundException;
import com.resourcebridge.auth_service.exceptions.RuntimeConflictException;
import com.resourcebridge.auth_service.repository.UserRepository;
import com.resourcebridge.auth_service.utils.PasswordUtils;
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

    // signup
    public UserResponseDto signup(SignupRequestDto dto){

        log.info("Signup request | email={}, | role={}",dto.getEmail(), dto.getRole());

        try{
            if(userRepository.existsByEmail(dto.getEmail())){
                log.warn("Signup blocked | email already exists: {}",dto.getEmail());
                throw new RuntimeConflictException("Email already registered");
            }
            // geocoding address
            log.debug("Geocoding address | email={}", dto.getEmail());

            Point location = geoCodingService.getLocation(dto.getAddress());

            // map dto -> entity
            User user = modelMapper.map(dto, User.class);

            user.setPassword(PasswordUtils.hashPassword(dto.getPassword()));

            user.setRole(dto.getRole());
            user.setLocation(location);
            user.setEnabled(true);
            user.setLocked(false);

            // verification
            if(dto.getRole()== Role.NGO || dto.getRole() == Role.CLINIC){
                user.setVerified(false);

                log.info("Verification pending | email={}| role={}",
                        dto.getEmail(), dto.getRole());
            }else {
                user.setVerified(true);
            }

            // save
            User saved = userRepository.save(user);

            log.info("User registered | id={} | role={}",saved.getId(), saved.getRole());

            return modelMapper.map(saved, UserResponseDto.class);
        }catch(Exception e){
            log.error("Signup failed | email={} | reason={}",
                    dto.getEmail(), e.getMessage(),e);
            throw e;
        }
    }

    // login
    public String login(LoginRequestDto dto){
        log.info("Login attempt | email={}",dto.getEmail());

        try{
            User user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(()->{
                        log.warn("Login failed | user not found | email={}",dto.getEmail());

                        return new ResourceNotFoundException("Invalid credentials");
                    });

            if(!PasswordUtils.checkPassword(dto.getPassword(),user.getPassword())){
                log.warn("Login failed | wrong password| email={}",
                        dto.getEmail());
                throw new BadCredentialsException("invalid credentials");
            }

            if(!user.isEnabled() || user.isLocked()){
                log.warn("Login blocked | account disabled | userId={}",user.getId());

                throw new RuntimeException("Account disabled");
            }

            String token = jwtService.generateAccessToken(user);

            log.info("Login success | userId={} | role={}",user.getId(), user.getRole());

            return token;
        }catch(Exception e){
            log.error("Login failed | email={} | reason={}",dto.getEmail(), e.getMessage());

            throw  e;
        }
    }

    // current user
    public UserResponseDto getCurrentUser(String authHeader){

        log.debug("Fetching current user from token");

        if(authHeader == null|| !authHeader.startsWith("Bearer ")){
            log.warn("Invalid Authorization header");

            throw new BadCredentialsException("Invalid token");
        }

        try{
            String token = authHeader.substring(7);

            Claims claims = jwtService.extractAllClaims(token);

            Long userId = claims.get("userId",Long.class);

            log.debug("Token validated | userId={}", userId);

            return getUserById(userId);
        }catch(Exception e){
            log.error("Jwt parsing failed | reason={}",e.getMessage());

            throw e;
        }
    }


    // get by id
    public UserResponseDto getUserById(Long id){
        log.debug("Fetching user |id={}",id);

        User user = userRepository.findById(id)
                .orElseThrow(()-> {
                    log.warn("User not found | id={}", id);

                    return new ResourceNotFoundException(
                            "User not found"
                    );
                });

        log.debug("User loaded | id={} | role={}",user.getId(),user.getRole());

        return modelMapper.map(user, UserResponseDto.class);
    }

    // verify user
    public void verifyUser(Long userId){
        log.info("Verification request for UserId={}",userId);

        User user = userRepository.findById(userId)
                .orElseThrow(()->
                        new ResourceNotFoundException("User  not found"));

        if(user.isVerified()){
            log.warn("User already verified | userId={}",userId);
            return;
        }

        user.setVerified(true);
        userRepository.save(user);

        log.info("User verified successfully | userId={}",userId);
    }
}
