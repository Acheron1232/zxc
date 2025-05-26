package com.laba.ilaba.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laba.ilaba.entity.Car;
import com.laba.ilaba.entity.Order;
import com.laba.ilaba.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testOrderResponseSerialization() throws Exception {
        // Create test entities
        Car car = Car.builder()
                .id(1L)
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .pricePerDay(new BigDecimal("50.00"))
                .isAvailable(true)
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("password")
                .role(User.Role.USER)
                .build();

        Order order = Order.builder()
                .id(1L)
                .carDto(car)
                .userDto(user)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .totalPrice(new BigDecimal("150.00"))
                .status(Order.Status.PENDING)
                .createdAt(LocalDate.now())
                .build();

        // Convert to OrderResponse
        OrderResponse orderResponse = OrderResponse.fromEntity(order);

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(orderResponse);
        System.out.println("[DEBUG_LOG] Serialized JSON: " + json);

        // Deserialize back to OrderResponse to verify
        OrderResponse deserializedResponse = objectMapper.readValue(json, OrderResponse.class);

        // Verify the deserialized object matches the original
        assertEquals(orderResponse.getId(), deserializedResponse.getId());
        assertEquals(orderResponse.getCar().getId(), deserializedResponse.getCar().getId());
        assertEquals(orderResponse.getUser().getId(), deserializedResponse.getUser().getId());
        assertEquals(orderResponse.getStartDate(), deserializedResponse.getStartDate());
        assertEquals(orderResponse.getEndDate(), deserializedResponse.getEndDate());
        assertEquals(orderResponse.getStatus(), deserializedResponse.getStatus());
        assertEquals(orderResponse.getTotalPrice(), deserializedResponse.getTotalPrice());
    }
}
