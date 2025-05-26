package com.laba.ilaba.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private Long carId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
}