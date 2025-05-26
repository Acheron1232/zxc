package com.laba.labais.dto;

import com.laba.labais.entity.Car;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarResponse {
    private Long id;
    private String make;
    private String model;
    private Integer year;
    private BigDecimal pricePerDay;
    private Boolean isAvailable;
    private String currentOrderId;

    private Long ownerId;
    private String ownerEmail;
    private String imageUrl;
    private String description;

    public static CarResponse fromEntity(Car car) {
        return CarResponse.builder()
                .id(car.getId())
                .make(car.getMake())
                .model(car.getModel())
                .year(car.getYear())
                .pricePerDay(car.getPricePerDay())
                .isAvailable(car.getIsAvailable())
                .currentOrderId(car.getCurrentOrderId())
                .build();
    }
}