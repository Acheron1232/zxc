package com.laba.ilaba.resource;

import com.laba.ilaba.dto.OrderRequest;
import com.laba.ilaba.dto.OrderResponse;
import com.laba.ilaba.service.OrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.List;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class OrderResource {
    
    @Inject
    private OrderService orderService;
    
    @GET
    public Response getAllOrders() {
        log.info("Getting all orders");
        try {
            List<OrderResponse> orders = orderService.getAllOrders();
            return Response.ok(orders).build();
        } catch (Exception e) {
            log.error("Error getting all orders", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
    
    @GET
    @Path("/{id}")
    public Response getOrderById(@PathParam("id") Long id) {
        log.info("Getting order by id: {}", id);
        try {
            OrderResponse order = orderService.getOrderById(id);
            return Response.ok(order).build();
        } catch (NotFoundException e) {
            log.warn("Order not found with id: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error getting order by id: {}", id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
    
//    @GET
//    @Path("/current-user-order")
//    public Response getCurrentUserOrder(@Context SecurityContext securityContext) {
//        Principal principal = securityContext.getUserPrincipal();
//        if (principal == null) {
//            log.warn("Attempt to get current user order without authentication");
//            return Response.status(Response.Status.UNAUTHORIZED).build();
//        }
//
//        String email = principal.getName();
//        log.info("Getting current order for user: {}", email);
//
//        try {
//            OrderResponse order = orderService.getCurrentUserOrder(email);
//            return Response.ok(order).build();
//        } catch (NotFoundException e) {
//            log.warn("No active order found for user: {}", email);
//            return Response.status(Response.Status.NOT_FOUND)
//                    .entity(e.getMessage())
//                    .build();
//        } catch (Exception e) {
//            log.error("Error getting current order for user: {}", email, e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                    .entity(e.getMessage())
//                    .build();
//        }
//    }
    
    @POST
    public Response createOrder(OrderRequest orderRequest, @Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            log.warn("Attempt to create order without authentication");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String email = principal.getName();
        log.info("Creating order for user: {}, carId: {}", email, orderRequest.getCarId());
        
        try {
            OrderResponse order = orderService.createOrder(orderRequest, email);
            return Response.status(Response.Status.CREATED).entity(order).build();
        } catch (BadRequestException e) {
            log.warn("Bad request creating order: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (NotFoundException e) {
            log.warn("Resource not found creating order: {}", e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error creating order for user: {}", email, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
    
    @PATCH
    @Path("/{id}")
    public Response updateOrderStatus(
            @PathParam("id") Long id,
            Status status,
            @Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            log.warn("Attempt to update order status without authentication");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String email = principal.getName();
        log.info("Updating order status: orderId={}, status={}, user={}", id, status, email);
        
        try {
            OrderResponse order = orderService.updateOrderStatus(id, status.status, email);
            return Response.ok(order).build();
        } catch (NotFoundException e) {
            log.warn("Order not found with id: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (ForbiddenException e) {
            log.warn("User {} not authorized to update order {}", email, id);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(e.getMessage())
                    .build();
        } catch (BadRequestException e) {
            log.warn("Bad request updating order status: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error updating order status: orderId={}, status={}, user={}", id, status, email, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
    private record Status(String status) {}
}