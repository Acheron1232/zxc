package com.laba.ilaba.service;

import com.laba.ilaba.dto.OrderRequest;
import com.laba.ilaba.dto.OrderResponse;
import com.laba.ilaba.entity.Car;
import com.laba.ilaba.entity.Order;
import com.laba.ilaba.entity.User;
import com.laba.ilaba.repository.CarRepository;
import com.laba.ilaba.repository.OrderRepository;
import com.laba.ilaba.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class OrderService {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private CarRepository carRepository;

    @Inject
    private CarService carService;

    public List<OrderResponse> getAllOrders() {
        log.info("Getting all orders");
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        log.info("Getting order by id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + id));
        return OrderResponse.fromEntity(order);
    }

    public List<OrderResponse> getOrdersByUser(String userEmail) {
        log.info("Getting orders for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));
        return orderRepository.findByUser(user).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public OrderResponse getCurrentUserOrder(String userEmail) {
        log.info("Getting current order for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        Order order = orderRepository.findCurrentOrderByUser(user)
                .orElseThrow(() -> new NotFoundException("No active order found for user: " + userEmail));

        return OrderResponse.fromEntity(order);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest, String userEmail) {
        log.info("Creating order for user: {}, carId: {}", userEmail, orderRequest.getCarId());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        Car car = carRepository.findById(orderRequest.getCarId())
                .orElseThrow(() -> new NotFoundException("Car not found with id: " + orderRequest.getCarId()));

        if (!car.getIsAvailable()) {
            log.warn("Attempted to order unavailable car: {}", car.getId());
            throw new BadRequestException("Car is not available for rent");
        }

        if (orderRequest.getStartDate().isBefore(LocalDate.now())) {
            log.warn("Attempted to create order with start date in the past: {}", orderRequest.getStartDate());
            throw new BadRequestException("Start date cannot be in the past");
        }

        if (orderRequest.getEndDate().isBefore(orderRequest.getStartDate())) {
            log.warn("Attempted to create order with end date before start date: start={}, end={}", 
                    orderRequest.getStartDate(), orderRequest.getEndDate());
            throw new BadRequestException("End date cannot be before start date");
        }

        Order order = Order.builder()
                .userDto(user)
                .carDto(car)
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
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + userEmail));

        // Only admin can update order status
//        if ( !status.toUpperCase().equals(Order.Status.PAID.toString()) || !status.toUpperCase().equals(Order.Status.REJECTED.toString())) {
//            log.warn("User {} attempted to update order {} but is not authorized", userEmail, orderId);
//            throw new ForbiddenException("You are not authorized to update this order");
//        }

        Order.Status newStatus;
        try {
            newStatus = Order.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order status: {}", status);
            throw new BadRequestException("Invalid order status: " + status);
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // If order is completed or rejected, make car available again
        if (newStatus == Order.Status.COMPLETED || newStatus == Order.Status.REJECTED) {
            carService.updateCarAvailability(order.getCarDto().getId(), true);

            // If order is rejected, detach the order from the car and user
            if (newStatus == Order.Status.REJECTED) {
                // Set the car's currentOrderId to null
                Car car = order.getCarDto();
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
