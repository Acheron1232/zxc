package com.laba.labais.service;

import com.laba.labais.dto.CarRequest;
import com.laba.labais.dto.CarResponse;
import com.laba.labais.entity.Car;
import com.laba.labais.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    private Car testCar;
    private CarRequest carRequest;

    @BeforeEach
    void setUp() {
        testCar = Car.builder()
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
    void getAllCars_ShouldReturnAllCars() {
        // Arrange
        Car car2 = Car.builder()
                .id(2L)
                .make("Honda")
                .model("Civic")
                .year(2021)
                .pricePerDay(new BigDecimal("40.00"))
                .isAvailable(true)
                .build();
                
        when(carRepository.findAll()).thenReturn(Arrays.asList(testCar, car2));

        // Act
        List<CarResponse> result = carService.getAllCars();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMake()).isEqualTo(testCar.getMake());
        assertThat(result.get(1).getMake()).isEqualTo(car2.getMake());
        
        verify(carRepository).findAll();
    }

    @Test
    void getCarById_ShouldReturnCar_WhenCarExists() {
        // Arrange
        when(carRepository.findById(anyLong())).thenReturn(Optional.of(testCar));

        // Act
        CarResponse result = carService.getCarById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testCar.getId());
        assertThat(result.getMake()).isEqualTo(testCar.getMake());
        assertThat(result.getModel()).isEqualTo(testCar.getModel());
        
        verify(carRepository).findById(1L);
    }

    @Test
    void getCarById_ShouldThrowException_WhenCarDoesNotExist() {
        // Arrange
        when(carRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            carService.getCarById(999L)
        );
        
        verify(carRepository).findById(999L);
    }

    @Test
    void getAvailableCars_ShouldReturnAvailableCars() {
        // Arrange
        Car car2 = Car.builder()
                .id(2L)
                .make("Honda")
                .model("Civic")
                .year(2021)
                .pricePerDay(new BigDecimal("40.00"))
                .isAvailable(true)
                .build();
                
        when(carRepository.findAvailableCars()).thenReturn(Arrays.asList(testCar, car2));

        // Act
        List<CarResponse> result = carService.getAvailableCars();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMake()).isEqualTo(testCar.getMake());
        assertThat(result.get(1).getMake()).isEqualTo(car2.getMake());
        
        verify(carRepository).findAvailableCars();
    }

    @Test
    void createCar_ShouldReturnCreatedCar() {
        // Arrange
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car savedCar = invocation.getArgument(0);
            savedCar.setId(3L);
            return savedCar;
        });

        // Act
        CarResponse result = carService.createCar(carRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getMake()).isEqualTo(carRequest.getMake());
        assertThat(result.getModel()).isEqualTo(carRequest.getModel());
        assertThat(result.getYear()).isEqualTo(carRequest.getYear());
        assertThat(result.getPricePerDay()).isEqualTo(carRequest.getPricePerDay());
        assertThat(result.getIsAvailable()).isTrue();
        
        verify(carRepository).save(any(Car.class));
    }

    @Test
    void deleteCar_ShouldDeleteCar_WhenCarExists() {
        // Arrange
        when(carRepository.findById(anyLong())).thenReturn(Optional.of(testCar));
        doNothing().when(carRepository).delete(any(Car.class));

        // Act
        carService.deleteCar(1L);

        // Assert
        verify(carRepository).findById(1L);
        verify(carRepository).delete(testCar);
    }

    @Test
    void deleteCar_ShouldThrowException_WhenCarDoesNotExist() {
        // Arrange
        when(carRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            carService.deleteCar(999L)
        );
        
        verify(carRepository).findById(999L);
        verify(carRepository, never()).delete(any(Car.class));
    }

    @Test
    void updateCarAvailability_ShouldUpdateAvailability_WhenCarExists() {
        // Arrange
        when(carRepository.findById(anyLong())).thenReturn(Optional.of(testCar));
        when(carRepository.save(any(Car.class))).thenReturn(testCar);

        // Act
        carService.updateCarAvailability(1L, false);

        // Assert
        assertThat(testCar.getIsAvailable()).isFalse();
        
        verify(carRepository).findById(1L);
        verify(carRepository).save(testCar);
    }

    @Test
    void updateCarAvailability_ShouldThrowException_WhenCarDoesNotExist() {
        // Arrange
        when(carRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            carService.updateCarAvailability(999L, false)
        );
        
        verify(carRepository).findById(999L);
        verify(carRepository, never()).save(any(Car.class));
    }
}