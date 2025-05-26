package com.laba.ilaba.resource;

import com.laba.ilaba.dto.CarRequest;
import com.laba.ilaba.dto.CarResponse;
import com.laba.ilaba.service.CarService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.List;

@Path("/cars")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class CarResource {
    
    @Inject
    private CarService carService;
    
    @GET
    public Response getAllCars() {
        log.info("Getting all cars");
        try {
            List<CarResponse> cars = carService.getAllCars();
            return Response.ok(cars).build();
        } catch (Exception e) {
            log.error("Error getting all cars", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
    
    @GET
    @Path("/{id}")
    public Response getCarById(@PathParam("id") Long id) {
        log.info("Getting car by id: {}", id);
        try {
            CarResponse car = carService.getCarById(id);
            return Response.ok(car).build();
        } catch (NotFoundException e) {
            log.warn("Car not found with id: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error getting car by id: {}", id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
    
    @POST
    public Response createCar(CarRequest carRequest, @Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            log.warn("Attempt to create car without authentication");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String email = principal.getName();
        log.info("Creating car for user: {}", email);
        
        try {
            CarResponse car = carService.createCar(carRequest);
            return Response.status(Response.Status.CREATED).entity(car).build();
        } catch (Exception e) {
            log.error("Error creating car for user: {}", email, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteCar(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            log.warn("Attempt to delete car without authentication");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        String email = principal.getName();
        log.info("Deleting car with id: {} by user: {}", id, email);
        
        try {
            carService.deleteCar(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            log.warn("Car not found with id: {}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (ForbiddenException e) {
            log.warn("User {} not authorized to delete car {}", email, id);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error deleting car with id: {} by user: {}", id, email, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
}