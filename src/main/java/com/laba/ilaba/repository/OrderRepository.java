package com.laba.ilaba.repository;

import com.laba.ilaba.entity.Car;
import com.laba.ilaba.entity.Order;
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
public class OrderRepository {
    @Inject
    private EntityManager entityManager;

    @Inject
    public OrderRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<Order> findById(Long id) {
        try {
            Order order = entityManager.find(Order.class, id);
            return Optional.ofNullable(order);
        } catch (Exception e) {
            log.error("Error finding order by id: {}", id, e);
            return Optional.empty();
        }
    }

    public List<Order> findAll() {
        try {
            return entityManager.createQuery("SELECT o FROM Order o", Order.class)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding all orders", e);
            return List.of();
        }
    }

    public List<Order> findByUser(User user) {
        try {
            return entityManager.createQuery(
                    "SELECT o FROM Order o WHERE o.userDto = :user", Order.class)
                    .setParameter("user", user)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding orders by user: {}", user, e);
            return List.of();
        }
    }

    public Optional<Order> findCurrentOrderByUser(User user) {
        try {
            List<Order.Status> statuses = List.of(Order.Status.PENDING, Order.Status.PAID, Order.Status.ACTIVE);
            List<Order> orders = entityManager.createQuery(
                    "SELECT o FROM Order o WHERE o.userDto = :user AND o.status IN :statuses", Order.class)
                    .setParameter("user", user)
                    .setParameter("statuses", statuses)
                    .getResultList();
            return orders.isEmpty() ? Optional.empty() : Optional.of(orders.get(0));
        } catch (Exception e) {
            log.error("Error finding current order by user: {}", user, e);
            return Optional.empty();
        }
    }

    public List<Order> findByCar(Car car) {
        try {
            return entityManager.createQuery(
                    "SELECT o FROM Order o WHERE o.carDto = :car", Order.class)
                    .setParameter("car", car)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding orders by car: {}", car, e);
            return List.of();
        }
    }

    public Order save(Order order) {
        try {
            if (order.getId() == null) {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                entityManager.persist(order);
                entityManager.flush();
                transaction.commit();
                return order;
            } else {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                Order mergedOrder = entityManager.merge(order);
                entityManager.flush();
                transaction.commit();
                return mergedOrder;
            }
        } catch (Exception e) {
            log.error("Error saving order: {}", order, e);
            throw e;
        }
    }

    public void delete(Order order) {
        try {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.remove(entityManager.contains(order) ? order : entityManager.merge(order));
            entityManager.flush();
            transaction.commit();
        } catch (Exception e) {
            log.error("Error deleting order: {}", order, e);
            throw e;
        }
    }

    public void updateOrderStatus(Long orderId, Order.Status status) {
        try {
            Order order = entityManager.find(Order.class, orderId);
            if (order != null) {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                order.setStatus(status);
                entityManager.merge(order);
                entityManager.flush();
                transaction.commit();
            }
        } catch (Exception e) {
            log.error("Error updating order status: orderId={}, status={}", orderId, status, e);
            throw e;
        }
    }
}
