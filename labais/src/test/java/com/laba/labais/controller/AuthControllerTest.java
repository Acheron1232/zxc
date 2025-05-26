package com.laba.labais.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laba.labais.dto.LoginRequest;
import com.laba.labais.dto.SignupRequest;
import com.laba.labais.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_ShouldReturnSuccess_WhenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        when(userService.login(any(LoginRequest.class))).thenReturn("jwt.token.string");

        // Act & Assert
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", "jwt.token.string"));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new UsernameNotFoundException("Invalid email or password"));

        // Act & Assert
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    void signup_ShouldReturnSuccess_WhenEmailIsUnique() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("new@example.com");
        signupRequest.setUsername("newuser");
        signupRequest.setPassword("password");

        when(userService.signup(any(SignupRequest.class))).thenReturn("jwt.token.string");

        // Act & Assert
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Signup successful"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", "jwt.token.string"));

        verify(userService).signup(any(SignupRequest.class));
    }

    @Test
    void signup_ShouldReturnBadRequest_WhenEmailExists() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("existing@example.com");
        signupRequest.setUsername("existinguser");
        signupRequest.setPassword("password");

        when(userService.signup(any(SignupRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));

        verify(userService).signup(any(SignupRequest.class));
    }

    @Test
    void logout_ShouldClearCookie() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().maxAge("accessToken", 0));
    }
}