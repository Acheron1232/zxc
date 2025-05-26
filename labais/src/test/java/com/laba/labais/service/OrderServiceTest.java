package com.laba.labais.service;

import com.laba.labais.dto.OrderRequest;
import com.laba.labais.dto.OrderResponse;
import com.laba.labais.entity.Car;
import com.laba.labais.entity.Order;
import com.laba.labais.entity.User;
import com.laba.labais.repository.CarRepository;
import com.laba.labais.repository.OrderRepository;
import com.laba.labais.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarService carService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Car testCar;
    private Order testOrder;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("encodedPassword")
                .role(User.Role.USER)
                .build();

        testCar = Car.builder()
                .id(1L)
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .pricePerDay(new BigDecimal("50.00"))
                .isAvailable(true)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .car(testCar)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(4))
                .totalPrice(new BigDecimal("150.00"))
                .status(Order.Status.PENDING)
                .createdAt(LocalDate.now())
                .build();

        orderRequest = new OrderRequest();
        orderRequest.setCarId(1L);
        orderRequest.setStartDate(LocalDate.now().plusDays(1));
        orderRequest.setEndDate(LocalDate.now().plusDays(4));
        orderRequest.setTotalPrice(new BigDecimal("150.00"));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        Order order2 = Order.builder()
                .id(2L)
                .user(testUser)
                .car(testCar)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(8))
                .totalPrice(new BigDecimal("150.00"))
                .status(Order.Status.PENDING)
                .createdAt(LocalDate.now())
                .build();

        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder, order2));

        // Act
        List<OrderResponse> result = orderService.getAllOrders();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(testOrder.getId());
        assertThat(result.get(1).getId()).isEqualTo(order2.getId());

        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenOrderExists() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));

        // Act
        OrderResponse result = orderService.getOrderById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testOrder.getId());
        assertThat(result.getUserDto().getId()).isEqualTo(testUser.getId());
        assertThat(result.getCarDto().getId()).isEqualTo(testCar.getId());

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_ShouldThrowException_WhenOrderDoesNotExist() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            orderService.getOrderById(999L)
        );

        verify(orderRepository).findById(999L);
    }

    @Test
    void getOrdersByUser_ShouldReturnUserOrders() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUser(any(User.class))).thenReturn(List.of(testOrder));

        // Act
        List<OrderResponse> result = orderService.getOrdersByUser("test@example.com");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testOrder.getId());

        verify(userRepository).findByEmail("test@example.com");
        verify(orderRepository).findByUser(testUser);
    }

    @Test
    void getOrdersByUser_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            orderService.getOrdersByUser("nonexistent@example.com")
        );

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(orderRepository, never()).findByUser(any(User.class));
    }

    @Test
    void getCurrentUserOrder_ShouldReturnCurrentOrder() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(orderRepository.findCurrentOrderByUser(any(User.class))).thenReturn(Optional.of(testOrder));

        // Act
        OrderResponse result = orderService.getCurrentUserOrder("test@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testOrder.getId());

        verify(userRepository).findByEmail("test@example.com");
        verify(orderRepository).findCurrentOrderByUser(testUser);
    }

    @Test
    void getCurrentUserOrder_ShouldThrowException_WhenNoActiveOrderExists() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(orderRepository.findCurrentOrderByUser(any(User.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            orderService.getCurrentUserOrder("test@example.com")
        );

        verify(userRepository).findByEmail("test@example.com");
        verify(orderRepository).findCurrentOrderByUser(testUser);
    }

    @Test
    void createOrder_ShouldCreateOrder_WhenValidRequest() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(carRepository.findById(anyLong())).thenReturn(Optional.of(testCar));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });
        doNothing().when(carService).updateCarAvailability(anyLong(), anyBoolean());

        // Act
        OrderResponse result = orderService.createOrder(orderRequest, "test@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserDto().getId()).isEqualTo(testUser.getId());
        assertThat(result.getCarDto().getId()).isEqualTo(testCar.getId());
        assertThat(result.getStatus()).isEqualTo(Order.Status.PENDING.name());

        verify(userRepository).findByEmail("test@example.com");
        verify(carRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(carService).updateCarAvailability(1L, false);
    }

    @Test
    void createOrder_ShouldThrowException_WhenCarIsNotAvailable() {
        // Arrange
        testCar.setIsAvailable(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(carRepository.findById(anyLong())).thenReturn(Optional.of(testCar));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            orderService.createOrder(orderRequest, "test@example.com")
        );

        verify(userRepository).findByEmail("test@example.com");
        verify(carRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(carService, never()).updateCarAvailability(anyLong(), anyBoolean());
    }

    @Test
    void createOrder_ShouldThrowException_WhenStartDateIsInPast() {
        // Arrange
        orderRequest.setStartDate(LocalDate.now().minusDays(1));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(carRepository.findById(anyLong())).thenReturn(Optional.of(testCar));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            orderService.createOrder(orderRequest, "test@example.com")
        );

        verify(userRepository).findByEmail("test@example.com");
        verify(carRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(carService, never()).updateCarAvailability(anyLong(), anyBoolean());
    }

    @Test
    void createOrder_ShouldThrowException_WhenEndDateIsBeforeStartDate() {
        // Arrange
        orderRequest.setEndDate(orderRequest.getStartDate().minusDays(1));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(carRepository.findById(anyLong())).thenReturn(Optional.of(testCar));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            orderService.createOrder(orderRequest, "test@example.com")
        );

        verify(userRepository).findByEmail("test@example.com");
        verify(carRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(carService, never()).updateCarAvailability(anyLong(), anyBoolean());
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus_WhenValidRequest() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse result = orderService.updateOrderStatus(1L, "PAID", "test@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testOrder.getId());
        assertThat(result.getStatus()).isEqualTo(Order.Status.PAID.name());

        verify(orderRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(orderRepository).save(testOrder);
        verify(carService, never()).updateCarAvailability(anyLong(), anyBoolean());
    }

    @Test
    void updateOrderStatus_ShouldMakeCarAvailable_WhenStatusIsCompleted() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(carService).updateCarAvailability(anyLong(), anyBoolean());

        // Act
        OrderResponse result = orderService.updateOrderStatus(1L, "COMPLETED", "test@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testOrder.getId());
        assertThat(result.getStatus()).isEqualTo(Order.Status.COMPLETED.name());

        verify(orderRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(orderRepository).save(testOrder);
        verify(carService).updateCarAvailability(1L, true);
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenInvalidStatus() {
        // Arrange
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(testOrder));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            orderService.updateOrderStatus(1L, "INVALID_STATUS", "test@example.com")
        );

        verify(orderRepository).findById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(orderRepository, never()).save(any(Order.class));
        verify(carService, never()).updateCarAvailability(anyLong(), anyBoolean());
    }
}
