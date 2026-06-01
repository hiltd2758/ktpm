
package com.e_health_care.web.security;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.e_health_care.web.admin.service.AdminDetailsService;
import com.e_health_care.web.admin.service.AdminJwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminJwtFilter extends OncePerRequestFilter {

    private final AdminJwtService jwtService;
    private final ApplicationContext context;

    public AdminJwtFilter(AdminJwtService jwtService,
                          ApplicationContext context) {
        this.jwtService = jwtService;
        this.context = context;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return !path.startsWith("/api/admin/") && !path.startsWith("/admin/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        String email = null;

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null &&
                authHeader.startsWith("Bearer ")) {

            token = authHeader.substring(7);
        }

        // Không có token -> bỏ qua
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Chỉ xử lý token ADMIN
        try {

            String role =
                    jwtService.extractRole(token);

            if (!"ROLE_ADMIN".equals(role)) {
                filterChain.doFilter(request, response);
                return;
            }

        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            email = jwtService.extractEmail(token);

            if (email != null
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        context.getBean(AdminDetailsService.class)
                                .loadUserByUsername(email);

                if (jwtService.validateToken(token, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);

                    request.setAttribute(
                            "adminToken",
                            token
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

}

