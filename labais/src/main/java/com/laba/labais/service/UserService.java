package com.laba.labais.service;

import com.laba.labais.dto.LoginRequest;
import com.laba.labais.dto.SignupRequest;
import com.laba.labais.dto.UserResponse;
import com.laba.labais.entity.User;
import com.laba.labais.repository.UserRepository;
import com.laba.labais.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserResponse getCurrentUser(String email) {
        log.info("Getting current user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
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
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public String login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getEmail());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        
        if (authentication.isAuthenticated()) {
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            log.info("User logged in successfully: {}", loginRequest.getEmail());
            return jwtUtil.generateToken(user);
        } else {
            throw new UsernameNotFoundException("Invalid email or password");
        }
    }

    @Transactional
    public String signup(SignupRequest signupRequest) {
        log.info("Attempting signup for user: {}", signupRequest.getEmail());

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            log.warn("Signup failed: Email already exists: {}", signupRequest.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
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