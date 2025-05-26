package com.laba.labais.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laba.labais.dto.CarResponse;
import com.laba.labais.dto.OrderRequest;
import com.laba.labais.dto.OrderResponse;
import com.laba.labais.dto.UserResponse;
import com.laba.labais.entity.Order;
import com.laba.labais.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private OrderResponse testOrderResponse;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDate serialization

        CarResponse carResponse = CarResponse.builder()
                .id(1L)
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .pricePerDay(new BigDecimal("50.00"))
                .isAvailable(true)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .role("USER")
                .build();

        testOrderResponse = OrderResponse.builder()
                .id(1L)
                .carDto(carResponse)
                .userDto(userResponse)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(4))
                .totalPrice(new BigDecimal("150.00"))
                .status("pending")
                .build();

        orderRequest = new OrderRequest();
        orderRequest.setCarId(1L);
        orderRequest.setStartDate(LocalDate.now().plusDays(1));
        orderRequest.setEndDate(LocalDate.now().plusDays(4));
        orderRequest.setTotalPrice(new BigDecimal("150.00"));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() throws Exception {
        // Arrange
        OrderResponse order2 = OrderResponse.builder()
                .id(2L)
                .carDto(testOrderResponse.getCarDto())
                .userDto(testOrderResponse.getUserDto())
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(8))
                .totalPrice(new BigDecimal("150.00"))
                .status("pending")
                .build();

        List<OrderResponse> orders = Arrays.asList(testOrderResponse, order2);
        when(orderService.getAllOrders()).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(orderService).getAllOrders();
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenOrderExists() throws Exception {
        // Arrange
        when(orderService.getOrderById(anyLong())).thenReturn(testOrderResponse);

        // Act & Assert
        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.carDto.id", is(1)))
                .andExpect(jsonPath("$.userDto.id", is(1)))
                .andExpect(jsonPath("$.status", is("pending")));

        verify(orderService).getOrderById(1L);
    }

    @Test
    void getOrderById_ShouldReturnNotFound_WhenOrderDoesNotExist() throws Exception {
        // Arrange
        when(orderService.getOrderById(anyLong())).thenThrow(new RuntimeException("Order not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());

        verify(orderService).getOrderById(999L);
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder_WhenAuthenticated() throws Exception {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(orderService.createOrder(any(OrderRequest.class), anyString())).thenReturn(testOrderResponse);

        // Act & Assert
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest))
                .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("pending")));

        verify(authentication).getName();
        verify(orderService).createOrder(any(OrderRequest.class), eq("test@example.com"));
    }

    @Test
    void createOrder_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).createOrder(any(OrderRequest.class), anyString());
    }

    @Test
    void createOrder_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(orderService.createOrder(any(OrderRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid request"));

        // Act & Assert
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest))
                .principal(authentication))
                .andExpect(status().isBadRequest());

        verify(authentication).getName();
        verify(orderService).createOrder(any(OrderRequest.class), eq("test@example.com"));
    }

    @Test
    void updateOrderStatus_ShouldReturnUpdatedOrder_WhenStatusIsValid() throws Exception {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");

        OrderResponse updatedOrder = OrderResponse.builder()
                .id(1L)
                .carDto(testOrderResponse.getCarDto())
                .userDto(testOrderResponse.getUserDto())
                .startDate(testOrderResponse.getStartDate())
                .endDate(testOrderResponse.getEndDate())
                .totalPrice(testOrderResponse.getTotalPrice())
                .status("paid")
                .build();

        when(orderService.updateOrderStatus(anyLong(), anyString(), anyString())).thenReturn(updatedOrder);

        // Create a status request object as a Map
        java.util.Map<String, String> statusRequest = new java.util.HashMap<>();
        statusRequest.put("status", "paid");

        // Act & Assert
        mockMvc.perform(patch("/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest))
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("paid")));

        verify(authentication).getName();
        verify(orderService).updateOrderStatus(eq(1L), eq("paid"), eq("test@example.com"));
    }

    @Test
    void updateOrderStatus_ShouldReturnBadRequest_WhenStatusIsInvalid() throws Exception {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(orderService.updateOrderStatus(anyLong(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid status"));

        // Create a status request object as a Map
        java.util.Map<String, String> statusRequest = new java.util.HashMap<>();
        statusRequest.put("status", "invalid_status");

        // Act & Assert
        mockMvc.perform(patch("/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest))
                .principal(authentication))
                .andExpect(status().isBadRequest());

        verify(authentication).getName();
        verify(orderService).updateOrderStatus(eq(1L), eq("invalid_status"), eq("test@example.com"));
    }

    @Test
    void updateOrderStatus_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Create a status request object as a Map
        java.util.Map<String, String> statusRequest = new java.util.HashMap<>();
        statusRequest.put("status", "paid");

        // Act & Assert
        mockMvc.perform(patch("/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).updateOrderStatus(anyLong(), anyString(), anyString());
    }
}
