package com.laba.labais.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laba.labais.dto.CarRequest;
import com.laba.labais.dto.CarResponse;
import com.laba.labais.service.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CarControllerTest {

    @Mock
    private CarService carService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CarController carController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CarResponse testCarResponse;
    private CarRequest carRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(carController).build();
        objectMapper = new ObjectMapper();

        testCarResponse = CarResponse.builder()
                .id(1L)
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .pricePerDay(new BigDecimal("50.00"))
                .isAvailable(true)
                .build();

        carRequest = new CarRequest();
        carRequest.setMake("Honda");
        carRequest.setModel("Accord");
        carRequest.setYear(2023);
        carRequest.setPricePerDay(new BigDecimal("60.00"));
    }

    @Test
    void getAllCars_ShouldReturnAllCars() throws Exception {
        // Arrange
        CarResponse car2 = CarResponse.builder()
                .id(2L)
                .make("Honda")
                .model("Civic")
                .year(2021)
                .pricePerDay(new BigDecimal("40.00"))
                .isAvailable(true)
                .build();

        List<CarResponse> cars = Arrays.asList(testCarResponse, car2);
        when(carService.getAllCars()).thenReturn(cars);

        // Act & Assert
        mockMvc.perform(get("/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].make", is("Toyota")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].make", is("Honda")));

        verify(carService).getAllCars();
    }

    @Test
    void getCarById_ShouldReturnCar_WhenCarExists() throws Exception {
        // Arrange
        when(carService.getCarById(anyLong())).thenReturn(testCarResponse);

        // Act & Assert
        mockMvc.perform(get("/cars/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.make", is("Toyota")))
                .andExpect(jsonPath("$.model", is("Camry")));

        verify(carService).getCarById(1L);
    }

    @Test
    void getCarById_ShouldReturnNotFound_WhenCarDoesNotExist() throws Exception {
        // Arrange
        when(carService.getCarById(anyLong())).thenThrow(new RuntimeException("Car not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/cars/999"))
                .andExpect(status().isNotFound());

        verify(carService).getCarById(999L);
    }

    @Test
    void createCar_ShouldReturnCreatedCar_WhenAuthenticated() throws Exception {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(carService.createCar(any(CarRequest.class))).thenReturn(testCarResponse);

        // Act & Assert
        mockMvc.perform(post("/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carRequest))
                .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.make", is("Toyota")));

        verify(authentication).getName();
        verify(carService).createCar(any(CarRequest.class));
    }

    @Test
    void createCar_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carRequest)))
                .andExpect(status().isUnauthorized());

        verify(carService, never()).createCar(any(CarRequest.class));
    }

    @Test
    void deleteCar_ShouldReturnNoContent_WhenCarExists() throws Exception {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        doNothing().when(carService).deleteCar(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/cars/1")
                .principal(authentication))
                .andExpect(status().isNoContent());

        verify(authentication).getName();
        verify(carService).deleteCar(1L);
    }

    @Test
    void deleteCar_ShouldReturnNotFound_WhenCarDoesNotExist() throws Exception {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        doThrow(new RuntimeException("Car not found with id: 999")).when(carService).deleteCar(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/cars/999")
                .principal(authentication))
                .andExpect(status().isNotFound());

        verify(authentication).getName();
        verify(carService).deleteCar(999L);
    }

    @Test
    void deleteCar_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/cars/1"))
                .andExpect(status().isUnauthorized());

        verify(carService, never()).deleteCar(anyLong());
    }
}