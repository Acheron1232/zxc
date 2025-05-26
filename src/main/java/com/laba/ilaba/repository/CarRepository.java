package com.laba.ilaba.repository;

import com.laba.ilaba.entity.Car;
import com.laba.ilaba.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class CarRepository {

    @Inject
    private EntityManager entityManager;

    @Inject
    public CarRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Car> findById(Long id) {
        try {
            Car car = entityManager.find(Car.class, id);
            return Optional.ofNullable(car);
        } catch (Exception e) {
            log.error("Error finding car by id: {}", id, e);
            return Optional.empty();
        }
    }

    public List<Car> findAll() {
        try {
            return entityManager.createQuery("SELECT c FROM Car c", Car.class)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding all cars", e);
            return List.of();
        }
    }

    public List<Car> findAvailableCars() {
        try {
            return entityManager.createQuery(
                    "SELECT c FROM Car c WHERE c.isAvailable = true", Car.class)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding available cars", e);
            return List.of();
        }
    }

    public Car save(Car car) {
        try {
            if (car.getId() == null) {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                entityManager.persist(car);
                entityManager.flush();
                transaction.commit();
                return car;
            } else {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                Car mergedCar = entityManager.merge(car);
                entityManager.flush();
                transaction.commit();
                return mergedCar;
            }
        } catch (Exception e) {
            log.error("Error saving car: {}", car, e);
            throw e;
        }
    }

    public void delete(Car car) {
        try {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.remove(entityManager.contains(car) ? car : entityManager.merge(car));
            entityManager.flush();
            transaction.commit();
        } catch (Exception e) {
            log.error("Error deleting car: {}", car, e);
            throw e;
        }
    }

    public void deleteById(Long id) {
        try {
            Car car = entityManager.find(Car.class, id);
            if (car != null) {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                entityManager.remove(car);
                entityManager.flush();
                transaction.commit();
            }
        } catch (Exception e) {
            log.error("Error deleting car by id: {}", id, e);
            throw e;
        }
    }
}
