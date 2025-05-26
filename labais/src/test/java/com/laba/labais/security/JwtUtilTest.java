package com.laba.labais.security;

import com.laba.labais.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "testsecrettestsecrettestsecrettestsecrettestsecrettestsecret");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("password")
                .role(User.Role.USER)
                .build();

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Act
        String token = jwtUtil.generateToken(testUser);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // Verify token contains expected username
        String username = jwtUtil.extractUsername(token);
        assertThat(username).isEqualTo(testUser.getEmail());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo(testUser.getEmail());
    }

    @Test
    void validateToken_ShouldReturnTrue_ForValidToken() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_ForExpiredToken() {
        // Arrange
        // Set expiration to a negative value to create an expired token
        ReflectionTestUtils.setField(jwtUtil, "expiration", -3600000L);
        String token = jwtUtil.generateToken(testUser);

        // Reset expiration for other tests
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);

        // Act
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_ForInvalidUsername() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        UserDetails wrongUserDetails = mock(UserDetails.class);
        when(wrongUserDetails.getUsername()).thenReturn("wrong@example.com");

        // Act
        boolean isValid = jwtUtil.validateToken(token, wrongUserDetails);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    void extractExpiration_ShouldReturnCorrectDate() {
        // Arrange
        String token = jwtUtil.generateToken(testUser);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        Date now = new Date();
        assertThat(expiration).isAfter(now);

        // Should be approximately 1 hour in the future (with some margin for test execution time)
        long diff = expiration.getTime() - now.getTime();
        assertThat(diff).isBetween(3500000L, 3600000L);
    }
}
