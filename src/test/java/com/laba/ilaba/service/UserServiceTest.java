package com.laba.ilaba.service;

import com.laba.ilaba.dto.LoginRequest;
import com.laba.ilaba.dto.SignupRequest;
import com.laba.ilaba.dto.UserResponse;
import com.laba.ilaba.entity.User;
import com.laba.ilaba.repository.UserRepository;
import com.laba.ilaba.security.JwtUtil;
import com.laba.ilaba.security.PasswordService;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCurrentUser_shouldReturnUser_whenUserExists() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .role(User.Role.USER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserResponse result = userService.getCurrentUser(email);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getRole().name(), result.getRole());

        verify(userRepository).findByEmail(email);
    }

    @Test
    void getCurrentUser_shouldThrowNotFoundException_whenUserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.getCurrentUser(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        String hashedPassword = "hashedPassword";
        String token = "jwt.token.here";

        LoginRequest loginRequest = new LoginRequest(email, password);

        User user = User.builder()
                .id(1L)
                .email(email)
                .password(hashedPassword)
                .role(User.Role.USER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordService.checkPassword(password, hashedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn(token);

        // Act
        String result = userService.login(loginRequest);

        // Assert
        assertEquals(token, result);
        verify(userRepository).findByEmail(email);
        verify(passwordService).checkPassword(password, hashedPassword);
        verify(jwtUtil).generateToken(user);
    }

    @Test
    void login_shouldThrowBadRequestException_whenCredentialsAreInvalid() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongPassword";
        String hashedPassword = "hashedPassword";

        LoginRequest loginRequest = new LoginRequest(email, password);

        User user = User.builder()
                .id(1L)
                .email(email)
                .password(hashedPassword)
                .role(User.Role.USER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordService.checkPassword(password, hashedPassword)).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.login(loginRequest));
        verify(userRepository).findByEmail(email);
        verify(passwordService).checkPassword(password, hashedPassword);
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void signup_shouldCreateUserAndReturnToken_whenEmailIsUnique() {
        // Arrange
        String email = "new@example.com";
        String password = "password";
        String hashedPassword = "hashedPassword";
        String token = "jwt.token.here";

        SignupRequest signupRequest = new SignupRequest("new_user",email, password);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordService.hashPassword(password)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });
        when(jwtUtil.generateToken(any(User.class))).thenReturn(token);

        // Act
        String result = userService.signup(signupRequest);

        // Assert
        assertEquals(token, result);
        verify(userRepository).existsByEmail(email);
        verify(passwordService).hashPassword(password);
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(any(User.class));
    }

    @Test
    void signup_shouldThrowBadRequestException_whenEmailAlreadyExists() {
        // Arrange
        String email = "existing@example.com";
        SignupRequest signupRequest = new SignupRequest("new_user", email, "password");

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.signup(signupRequest));
        verify(userRepository).existsByEmail(email);
        verify(passwordService, never()).hashPassword(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtil, never()).generateToken(any(User.class));
    }
}
