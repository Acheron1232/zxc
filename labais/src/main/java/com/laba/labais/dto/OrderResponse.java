package com.laba.labais.dto;

import com.laba.labais.entity.Order;
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
    private CarResponse carDto;
    private UserResponse userDto;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // "pending", "paid", etc.
    private BigDecimal totalPrice;

    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .carDto(order.getCar() != null ? CarResponse.fromEntity(order.getCar()) : null)
                .userDto(order.getUser() != null ? UserResponse.fromEntity(order.getUser()) : null)
                .startDate(order.getStartDate())
                .endDate(order.getEndDate())
                .status(order.getStatus().name().toLowerCase()) // to match TS enum style
                .totalPrice(order.getTotalPrice())
                .build();
    }
}
