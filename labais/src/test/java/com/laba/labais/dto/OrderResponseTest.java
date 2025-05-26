package com.laba.labais.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laba.labais.entity.Car;
import com.laba.labais.entity.Order;
import com.laba.labais.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class OrderResponseTest {

    @Autowired
    private ObjectMapper objectMapper;

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
                .car(car)
                .user(user)
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
        assertThat(deserializedResponse.getId()).isEqualTo(orderResponse.getId());
        assertThat(deserializedResponse.getCar().getId()).isEqualTo(orderResponse.getCar().getId());
        assertThat(deserializedResponse.getUser().getId()).isEqualTo(orderResponse.getUser().getId());
        assertThat(deserializedResponse.getStartDate()).isEqualTo(orderResponse.getStartDate());
        assertThat(deserializedResponse.getEndDate()).isEqualTo(orderResponse.getEndDate());
        assertThat(deserializedResponse.getStatus()).isEqualTo(orderResponse.getStatus());
        assertThat(deserializedResponse.getTotalPrice()).isEqualTo(orderResponse.getTotalPrice());
    }
}