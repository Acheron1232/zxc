package com.laba.labais.service;

import com.laba.labais.dto.CarRequest;
import com.laba.labais.dto.CarResponse;
import com.laba.labais.entity.Car;
import com.laba.labais.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarService {

    private final CarRepository carRepository;

    public List<CarResponse> getAllCars() {
        log.info("Getting all cars");
        return carRepository.findAll().stream()
                .map(CarResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public CarResponse getCarById(Long id) {
        log.info("Getting car by id: {}", id);
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));
        return CarResponse.fromEntity(car);
    }

    public List<CarResponse> getAvailableCars() {
        log.info("Getting available cars");
        return carRepository.findAvailableCars().stream()
                .map(CarResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CarResponse createCar(CarRequest carRequest) {
        log.info("Creating car");

        Car car = Car.builder()
                .make(carRequest.getMake())
                .model(carRequest.getModel())
                .year(carRequest.getYear())
                .pricePerDay(carRequest.getPricePerDay())
                .isAvailable(true)
                .build();

        Car savedCar = carRepository.save(car);
        log.info("Car created successfully with id: {}", savedCar.getId());
        return CarResponse.fromEntity(savedCar);
    }

    @Transactional
    public void deleteCar(Long id) {
        log.info("Deleting car with id: {}", id);
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));
        carRepository.delete(car);
        log.info("Car deleted successfully with id: {}", id);
    }

    @Transactional
    public void updateCarAvailability(Long id, boolean available) {
        log.info("Updating car availability: id={}, available={}", id, available);
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));

        car.setIsAvailable(available);
        carRepository.save(car);
        log.info("Car availability updated successfully: id={}, available={}", id, available);
    }
}