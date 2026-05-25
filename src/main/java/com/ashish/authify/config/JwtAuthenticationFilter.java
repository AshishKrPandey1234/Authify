package com.ashish.authify.config;

import com.ashish.authify.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. If no Bearer token found, skip this filter and move to the next one
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract token string (skip "Bearer " which is 7 characters)
        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractEmail(jwt);

            // 3. If email is extracted and user is not already authenticated in this request
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 4. If token is valid, create an authentication token for Spring Security context
                if (jwtService.validateToken(jwt, userEmail)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            Collections.emptyList() // Empty list means no special roles/authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. Update the Security Context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // If token is tampered or expired, don't authenticate, let it fail at security config layer
            System.out.println("JWT Verification failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}