package com.cams.account.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        System.out.println("====================================");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Authorization header present: "
                + (authorizationHeader != null));

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            System.out.println("JWT NOT FOUND");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        System.out.println("Bearer token received: YES");

        try {

            boolean valid = jwtService.isTokenValid(token);

            System.out.println("JWT valid: " + valid);

            if (valid) {

                String username =
                        jwtService.extractUsername(token);

                String role =
                        jwtService.extractRole(token);

                System.out.println("Username: " + username);
                System.out.println("Role: " + role);

                var authority =
                        new SimpleGrantedAuthority(
                                "ROLE_" + role);

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(authority));

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

                System.out.println(
                        "Authentication set successfully");
            }

        } catch (Exception e) {

            System.out.println(
                    "JWT ERROR: " + e.getClass().getName());

            System.out.println(
                    "JWT ERROR MESSAGE: " + e.getMessage());

            SecurityContextHolder.clearContext();
        }

        System.out.println(
                "Authenticated: " +
                        (SecurityContextHolder.getContext()
                                .getAuthentication() != null));

        System.out.println("====================================");

        filterChain.doFilter(request, response);
    }
}