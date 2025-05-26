package com.laba.ilaba.dto;

import com.laba.ilaba.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private CarResponse car;
    private UserResponse user;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // "pending", "paid", etc.
    private BigDecimal totalPrice;

    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .car(order.getCarDto() != null ? CarResponse.fromEntity(order.getCarDto()) : null)
                .user(order.getUserDto() != null ? UserResponse.fromEntity(order.getUserDto()) : null)
                .startDate(order.getStartDate())
                .endDate(order.getEndDate())
                .status(order.getStatus().name().toLowerCase()) // to match TS enum style
                .totalPrice(order.getTotalPrice())
                .build();
    }
}
