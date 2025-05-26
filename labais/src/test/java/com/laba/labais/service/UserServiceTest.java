package com.laba.labais.service;

import com.laba.labais.dto.LoginRequest;
import com.laba.labais.dto.SignupRequest;
import com.laba.labais.dto.UserResponse;
import com.laba.labais.entity.User;
import com.laba.labais.repository.UserRepository;
import com.laba.labais.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("encodedPassword")
                .role(User.Role.USER)
                .build();
    }

    @Test
    void getCurrentUser_ShouldReturnUserResponse_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.getCurrentUser("test@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(result.getRole()).isEqualTo(testUser.getRole().name());
        
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getCurrentUser_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> 
            userService.getCurrentUser("nonexistent@example.com")
        );
        
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .username("user2")
                .password("encodedPassword")
                .role(User.Role.USER)
                .build();
                
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.get(1).getEmail()).isEqualTo(user2.getEmail());
        
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_ShouldReturnUserResponse_WhenUserExists() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.getUserById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> 
            userService.getUserById(999L)
        );
        
        verify(userRepository).findById(999L);
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt.token.string");

        // Act
        String result = userService.login(loginRequest);

        // Assert
        assertThat(result).isEqualTo("jwt.token.string");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtUtil).generateToken(testUser);
    }

    @Test
    void login_ShouldThrowException_WhenAuthenticationFails() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> 
            userService.login(loginRequest)
        );
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    void signup_ShouldReturnToken_WhenEmailIsUnique() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("new@example.com");
        signupRequest.setUsername("newuser");
        signupRequest.setPassword("password");
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(2L);
            return savedUser;
        });
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt.token.string");

        // Act
        String result = userService.signup(signupRequest);

        // Assert
        assertThat(result).isEqualTo("jwt.token.string");
        
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(any(User.class));
    }

    @Test
    void signup_ShouldThrowException_WhenEmailExists() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("existing@example.com");
        signupRequest.setUsername("existinguser");
        signupRequest.setPassword("password");
        
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            userService.signup(signupRequest)
        );
        
        verify(userRepository).existsByEmail("existing@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    void exists_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act
        boolean result = userService.exists("existing@example.com");

        // Assert
        assertThat(result).isTrue();
        
        verify(userRepository).existsByEmail("existing@example.com");
    }

    @Test
    void exists_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Act
        boolean result = userService.exists("nonexistent@example.com");

        // Assert
        assertThat(result).isFalse();
        
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }
}