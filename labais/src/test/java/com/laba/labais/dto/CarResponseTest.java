package com.laba.labais.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laba.labais.entity.Car;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CarResponseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCarResponseSerialization() throws Exception {
        // Create test entity
        Car car = Car.builder()
                .id(1L)
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .pricePerDay(new BigDecimal("50.00"))
                .isAvailable(true)
                .currentOrderId("123")
                .build();

        // Convert to CarResponse
        CarResponse carResponse = CarResponse.fromEntity(car);

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(carResponse);
        System.out.println("[DEBUG_LOG] Serialized JSON: " + json);

        // Deserialize back to CarResponse to verify
        CarResponse deserializedResponse = objectMapper.readValue(json, CarResponse.class);

        // Verify the deserialized object matches the original
        assertThat(deserializedResponse.getId()).isEqualTo(carResponse.getId());
        assertThat(deserializedResponse.getMake()).isEqualTo(carResponse.getMake());
        assertThat(deserializedResponse.getModel()).isEqualTo(carResponse.getModel());
        assertThat(deserializedResponse.getYear()).isEqualTo(carResponse.getYear());
        assertThat(deserializedResponse.getPricePerDay()).isEqualTo(carResponse.getPricePerDay());
        assertThat(deserializedResponse.getIsAvailable()).isEqualTo(carResponse.getIsAvailable());
        assertThat(deserializedResponse.getCurrentOrderId()).isEqualTo(carResponse.getCurrentOrderId());
    }

    @Test
    public void testFromEntity() {
        // Create test entity
        Car car = Car.builder()
                .id(1L)
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .pricePerDay(new BigDecimal("50.00"))
                .isAvailable(true)
                .currentOrderId("123")
                .build();

        // Convert to CarResponse
        CarResponse carResponse = CarResponse.fromEntity(car);

        // Verify the conversion
        assertThat(carResponse.getId()).isEqualTo(car.getId());
        assertThat(carResponse.getMake()).isEqualTo(car.getMake());
        assertThat(carResponse.getModel()).isEqualTo(car.getModel());
        assertThat(carResponse.getYear()).isEqualTo(car.getYear());
        assertThat(carResponse.getPricePerDay()).isEqualTo(car.getPricePerDay());
        assertThat(carResponse.getIsAvailable()).isEqualTo(car.getIsAvailable());
        assertThat(carResponse.getCurrentOrderId()).isEqualTo(car.getCurrentOrderId());
        
        // Verify that optional fields are null
        assertThat(carResponse.getOwnerId()).isNull();
        assertThat(carResponse.getOwnerEmail()).isNull();
        assertThat(carResponse.getImageUrl()).isNull();
        assertThat(carResponse.getDescription()).isNull();
    }
}