package com.laba.ilaba.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarRequest {
    private String make;
    private String model;
    private Integer year;
    private BigDecimal pricePerDay;
}
