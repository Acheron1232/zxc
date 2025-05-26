package com.laba.ilaba.resource;

import com.laba.ilaba.dto.OrderResponse;
import com.laba.ilaba.dto.UserResponse;
import com.laba.ilaba.service.OrderService;
import com.laba.ilaba.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class UserResource {
    
    @Inject
    private UserService userService;
    @Inject
    private OrderService orderService;


    @GET
    @Path("/current-user-order")
    public Response getCurrentUserOrder(@Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            log.warn("Attempt to get current user order without authentication");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String email = principal.getName();
        log.info("Getting current order for user: {}", email);

        try {
            OrderResponse order = orderService.getCurrentUserOrder(email);
            return Response.ok(order).build();
        } catch (NotFoundException e) {
            log.warn("No active order found for user: {}", email);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error getting current order for user: {}", email, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
    
    @GET
    @Path("/current-user")
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            log.warn("Attempt to get current user without authentication");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String email = principal.getName();
        log.info("Getting current user for email: {}", email);
        
        try {
            UserResponse userResponse = userService.getCurrentUser(email);
            return Response.ok(userResponse).build();
        } catch (Exception e) {
            log.error("Error getting current user for email: {}", email, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
}