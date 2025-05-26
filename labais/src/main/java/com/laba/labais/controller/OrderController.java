package com.laba.labais.controller;

import com.laba.labais.dto.OrderRequest;
import com.laba.labais.dto.OrderResponse;
import com.laba.labais.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Getting all orders");
        try {
            List<OrderResponse> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting all orders", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        log.info("Getting order by id: {}", id);
        try {
            OrderResponse order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.warn("Order not found with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting order by id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest, Authentication authentication) {
        if (authentication == null) {
            log.warn("Attempt to create order without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        log.info("Creating order for user: {}, carId: {}", email, orderRequest.getCarId());

        try {
            OrderResponse order = orderService.createOrder(orderRequest, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalArgumentException e) {
            log.warn("Bad request creating order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            log.warn("Resource not found creating order: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error creating order for user: {}", email, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest status,
            Authentication authentication) {
        if (authentication == null) {
            log.warn("Attempt to update order status without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        log.info("Updating order status: orderId={}, status={}, user={}", id, status.getStatus(), email);

        try {
            OrderResponse order = orderService.updateOrderStatus(id, status.getStatus(), email);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException) {
                log.warn("Bad request updating order status: {}", e.getMessage());
                return ResponseEntity.badRequest().build();
            } else {
                log.warn("Order not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating order status: orderId={}, status={}, user={}", id, status, email, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Inner class for status request
    private static class StatusRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
