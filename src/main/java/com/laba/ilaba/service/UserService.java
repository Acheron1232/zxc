package com.laba.ilaba.service;

import com.laba.ilaba.dto.LoginRequest;
import com.laba.ilaba.dto.SignupRequest;
import com.laba.ilaba.dto.UserResponse;
import com.laba.ilaba.entity.User;
import com.laba.ilaba.repository.UserRepository;
import com.laba.ilaba.security.JwtUtil;
import com.laba.ilaba.security.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private PasswordService passwordService;
    
    @Inject
    private JwtUtil jwtUtil;
    
    public UserResponse getCurrentUser(String email) {
        log.info("Getting current user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return UserResponse.fromEntity(user);
    }
    
    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public UserResponse getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }
    
    @Transactional
    public String login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getEmail());
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        
        if (!passwordService.checkPassword(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", loginRequest.getEmail());
            throw new BadRequestException("Invalid email or password");
        }
        
        log.info("User logged in successfully: {}", loginRequest.getEmail());
        return jwtUtil.generateToken(user);
    }
    
    @Transactional
    public String signup(SignupRequest signupRequest) {
        log.info("Attempting signup for user: {}", signupRequest.getEmail());
        
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            log.warn("Signup failed: Email already exists: {}", signupRequest.getEmail());
            throw new BadRequestException("Email already exists");
        }
        
        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordService.hashPassword(signupRequest.getPassword()))
                .role(User.Role.USER)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("User signed up successfully: {}", signupRequest.getEmail());
        
        return jwtUtil.generateToken(savedUser);
    }
    
    public boolean exists(String email) {
        return userRepository.existsByEmail(email);
    }
}