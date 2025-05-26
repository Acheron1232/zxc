package com.laba.labais.repository;

import com.laba.labais.entity.Car;
import com.laba.labais.entity.Order;
import com.laba.labais.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    
    List<Order> findByCar(Car car);
    
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status IN :statuses")
    List<Order> findByUserAndStatusIn(@Param("user") User user, @Param("statuses") List<Order.Status> statuses);
    
    default Optional<Order> findCurrentOrderByUser(User user) {
        List<Order.Status> activeStatuses = List.of(Order.Status.PENDING, Order.Status.PAID, Order.Status.ACTIVE);
        List<Order> orders = findByUserAndStatusIn(user, activeStatuses);
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.get(0));
    }
}