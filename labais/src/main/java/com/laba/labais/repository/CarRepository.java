package com.laba.labais.repository;

import com.laba.labais.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByIsAvailableTrue();
    
    @Query("SELECT c FROM Car c WHERE c.isAvailable = true")
    List<Car> findAvailableCars();
}