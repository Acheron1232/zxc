package com.laba.labais.controller;

import com.laba.labais.dto.CarRequest;
import com.laba.labais.dto.CarResponse;
import com.laba.labais.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
@Slf4j
public class CarController {

    private final CarService carService;

    @GetMapping
    public ResponseEntity<List<CarResponse>> getAllCars() {
        log.info("Getting all cars");
        try {
            List<CarResponse> cars = carService.getAllCars();
            return ResponseEntity.ok(cars);
        } catch (Exception e) {
            log.error("Error getting all cars", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Long id) {
        log.info("Getting car by id: {}", id);
        try {
            CarResponse car = carService.getCarById(id);
            return ResponseEntity.ok(car);
        } catch (RuntimeException e) {
            log.warn("Car not found with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting car by id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<CarResponse> createCar(@RequestBody CarRequest carRequest, Authentication authentication) {
        if (authentication == null) {
            log.warn("Attempt to create car without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        log.info("Creating car for user: {}", email);

        try {
            CarResponse car = carService.createCar(carRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(car);
        } catch (Exception e) {
            log.error("Error creating car for user: {}", email, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            log.warn("Attempt to delete car without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        log.info("Deleting car with id: {} by user: {}", id, email);

        try {
            carService.deleteCar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Car not found with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting car with id: {} by user: {}", id, email, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
