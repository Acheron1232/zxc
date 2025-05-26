package com.laba.labais.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Extract JWT token from cookie
        String token = extractTokenFromCookie(request);
        
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = jwtUtil.extractUsername(token);
            
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies == null) {
            return null;
        }
        
        Optional<Cookie> tokenCookie = Arrays.stream(cookies)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .findFirst();
                
        return tokenCookie.map(Cookie::getValue).orElse(null);
    }
}