package com.laba.ilaba.security;


import com.laba.ilaba.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Provider
public class JwtFilter implements ContainerRequestFilter {
    @Inject
    private JwtUtil jwtUtil;
    @Inject
    private UserService userService;
//    @Context
//    private SecurityContext securityContext;


    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String path = containerRequestContext.getUriInfo().getPath();
        System.out.println(path);
        if (path.contains("login") || path.contains("signup") || (path.contains("orders") &&
                containerRequestContext.getMethod().equals("OPTIONS")) || (path.contains("cars") &&
                containerRequestContext.getMethod().equals("OPTIONS"))) {
            return;
        }
        String jwt = getTokenFromHeader(containerRequestContext);

        if (jwt != null && !jwt.isBlank() && jwtUtil.isTokenExpired(jwt)) {
            String username = jwtUtil.getUsername(jwt);
            boolean exists = userService.exists(username);
            if (!exists) {
                containerRequestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                                .entity("User " + username + " does not exist")
                                .build()
                );
            } else {
                containerRequestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return new Principal() {

                            @Override
                            public String getName() {
                                return username;
                            }
                        };
                    }

                    @Override
                    public boolean isUserInRole(String s) {
                        return false;
                    }

                    @Override
                    public boolean isSecure() {
                        return false;
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return "";
                    }
                });
            }
        } else {
            containerRequestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Jwt not valid")
                            .build()
            );
        }
    }

    private String getTokenFromHeader(ContainerRequestContext containerRequestContext) {
        try {
            Map<String, Cookie> cookies = containerRequestContext.getCookies();
            Cookie accessToken = cookies.get("accessToken");
            return accessToken.getValue();
        } catch (RuntimeException e) {
            containerRequestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("failed to parse the token")
                            .build()
            );
            return null;
        }
    }
}
