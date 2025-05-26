package com.laba.labais.controller;

import com.laba.labais.dto.LoginRequest;
import com.laba.labais.dto.SignupRequest;
import com.laba.labais.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Login request received for email: {}", loginRequest.getEmail());
        try {
            String token = userService.login(loginRequest);

            Cookie cookie = new Cookie("accessToken", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return ResponseEntity.ok("Login successful");
        } catch (Exception e) {
            log.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest signupRequest, HttpServletResponse response) {
        log.info("Signup request received for email: {}", signupRequest.getEmail());
        try {
            String token = userService.signup(signupRequest);

            Cookie cookie = new Cookie("accessToken", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return ResponseEntity.ok("Signup successful");
        } catch (Exception e) {
            log.error("Signup failed for email: {}", signupRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        log.info("Logout request received");

        Cookie cookie = new Cookie("accessToken", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseEntity.ok("Logout successful");
    }
}
