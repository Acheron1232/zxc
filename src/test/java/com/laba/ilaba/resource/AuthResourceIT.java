package com.laba.ilaba.resource;

import com.laba.ilaba.dto.LoginRequest;
import com.laba.ilaba.dto.SignupRequest;
import com.laba.ilaba.service.UserService;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthResourceIT {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthResource authResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_shouldReturnOk_whenCredentialsAreValid() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        String token = "jwt.token.here";

        when(userService.login(any(LoginRequest.class))).thenReturn(token);

        // Act
        Response response = authResource.login(loginRequest);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Login successful", response.getEntity());

        // Check if cookie is set
        NewCookie cookie = (NewCookie) response.getCookies().get("accessToken");
        assertEquals(token, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    void signup_shouldReturnOk_whenSignupIsSuccessful() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest(
                "testuser", "new@example.com", "password");
        String token = "jwt.token.here";

        when(userService.signup(any(SignupRequest.class))).thenReturn(token);

        // Act
        Response response = authResource.signup(signupRequest);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Signup successful", response.getEntity());

        // Check if cookie is set
        NewCookie cookie = (NewCookie) response.getCookies().get("accessToken");
        assertEquals(token, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    void logout_shouldReturnOk() {
        // Act
        Response response = authResource.logout();

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Logout successful", response.getEntity());

        // Check if cookie is cleared
        NewCookie cookie = (NewCookie) response.getCookies().get("accessToken");
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
    }
}
