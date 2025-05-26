package com.laba.ilaba.resource;

import com.laba.ilaba.dto.LoginRequest;
import com.laba.ilaba.dto.SignupRequest;
import com.laba.ilaba.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class AuthResource {

    @Inject
    private UserService userService;

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        log.info("Login request received for email: {}", loginRequest.getEmail());
        try {
            String token = userService.login(loginRequest);
            NewCookie cookie = new NewCookie.Builder("accessToken")
                    .value(token)
                    .path("/")
                    .httpOnly(true)
                    .build();
            return Response.ok()
                    .cookie(cookie)
                    .entity("Login successful")
                    .build();
        } catch (Exception e) {
            log.error("Login failed for email: {}", loginRequest.getEmail(), e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/signup")
    public Response signup(SignupRequest signupRequest) {
        log.info("Signup request received for email: {}", signupRequest.getEmail());
        try {
            String token = userService.signup(signupRequest);
            NewCookie cookie = new NewCookie.Builder("accessToken")
                    .value(token)
                    .path("/")
                    .httpOnly(true)
                    .build();
            return Response.ok()
                    .cookie(cookie)
                    .entity("Signup successful")
                    .build();
        } catch (Exception e) {
            log.error("Signup failed for email: {}", signupRequest.getEmail(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/logout")
    public Response logout() {
        log.info("Logout request received");
        // In a stateless JWT-based authentication system, logout is typically handled client-side
        // by removing the token from storage. However, we also clear the cookie on the server side.
        NewCookie cookie = new NewCookie.Builder("accessToken")
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        return Response.ok()
                .cookie(cookie)
                .entity("Logout successful")
                .build();
    }
}
