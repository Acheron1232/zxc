package com.laba.labais.controller;

import com.laba.labais.dto.OrderResponse;
import com.laba.labais.dto.UserResponse;
import com.laba.labais.service.OrderService;
import com.laba.labais.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/current-user-order")
    public ResponseEntity<OrderResponse> getCurrentUserOrder(Authentication authentication) {
        if (authentication == null) {
            log.warn("Attempt to get current user order without authentication");
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        log.info("Getting current order for user: {}", email);

        try {
            OrderResponse order = orderService.getCurrentUserOrder(email);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.warn("No active order found for user: {}", email);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            log.warn("Attempt to get current user without authentication");
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        log.info("Getting current user for email: {}", email);

        try {
            UserResponse userResponse = userService.getCurrentUser(email);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            log.error("Error getting current user for email: {}", email, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
