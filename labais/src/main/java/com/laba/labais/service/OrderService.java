package com.laba.labais.service;

import com.laba.labais.dto.OrderRequest;
import com.laba.labais.dto.OrderResponse;
import com.laba.labais.entity.Car;
import com.laba.labais.entity.Order;
import com.laba.labais.entity.User;
import com.laba.labais.repository.CarRepository;
import com.laba.labais.repository.OrderRepository;
import com.laba.labais.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final CarService carService;

    public List<OrderResponse> getAllOrders() {
        log.info("Getting all orders");
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        log.info("Getting order by id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return OrderResponse.fromEntity(order);
    }

    public List<OrderResponse> getOrdersByUser(String userEmail) {
        log.info("Getting orders for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        return orderRepository.findByUser(user).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public OrderResponse getCurrentUserOrder(String userEmail) {
        log.info("Getting current order for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Order order = orderRepository.findCurrentOrderByUser(user)
                .orElseThrow(() -> new RuntimeException("No active order found for user: " + userEmail));

        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest, String userEmail) {
        log.info("Creating order for user: {}, carId: {}", userEmail, orderRequest.getCarId());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Car car = carRepository.findById(orderRequest.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + orderRequest.getCarId()));

        if (!car.getIsAvailable()) {
            log.warn("Attempted to order unavailable car: {}", car.getId());
            throw new IllegalArgumentException("Car is not available for rent");
        }

        if (orderRequest.getStartDate().isBefore(LocalDate.now())) {
            log.warn("Attempted to create order with start date in the past: {}", orderRequest.getStartDate());
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        if (orderRequest.getEndDate().isBefore(orderRequest.getStartDate())) {
            log.warn("Attempted to create order with end date before start date: start={}, end={}",
                    orderRequest.getStartDate(), orderRequest.getEndDate());
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Order order = Order.builder()
                .user(user)
                .car(car)
                .startDate(orderRequest.getStartDate())
                .endDate(orderRequest.getEndDate())
                .totalPrice(orderRequest.getTotalPrice())
                .status(Order.Status.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Mark car as unavailable
        carService.updateCarAvailability(car.getId(), false);

        log.info("Order created successfully with id: {}", savedOrder.getId());
        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status, String userEmail) {
        log.info("Updating order status: orderId={}, status={}, user={}", orderId, status, userEmail);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Order.Status newStatus;
        try {
            newStatus = Order.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order status: {}", status);
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // If order is completed or rejected, make car available again
        if (newStatus == Order.Status.COMPLETED || newStatus == Order.Status.REJECTED) {
            carService.updateCarAvailability(order.getCar().getId(), true);

            // If order is rejected, detach the order from the car and user
            if (newStatus == Order.Status.REJECTED) {
                // Set the car's currentOrderId to null
                Car car = order.getCar();
                car.setCurrentOrderId(null);
                carRepository.save(car);

                // Create a new order without car and user references
                Order detachedOrder = Order.builder()
                        .id(order.getId())
                        .startDate(order.getStartDate())
                        .endDate(order.getEndDate())
                        .totalPrice(order.getTotalPrice())
                        .status(Order.Status.REJECTED)
                        .createdAt(order.getCreatedAt())
                        .build();

                // Save the detached order
                updatedOrder = orderRepository.save(detachedOrder);
                return OrderResponse.fromEntity(updatedOrder);
            }
        }

        log.info("Order status updated successfully: orderId={}, status={}", orderId, newStatus);
        return OrderResponse.fromEntity(updatedOrder);
    }
}