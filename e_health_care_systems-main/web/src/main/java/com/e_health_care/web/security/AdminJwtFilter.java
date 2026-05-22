package com.e_health_care.web.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.e_health_care.web.admin.service.AdminDetailsService;
import com.e_health_care.web.admin.service.AdminJwtService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminJwtFilter extends OncePerRequestFilter{
    private final AdminJwtService jwtService;
    private final ApplicationContext context;
    
    // Add a constructor for dependency injection
    public AdminJwtFilter(AdminJwtService jwtService, ApplicationContext context) {
        this.jwtService = jwtService;
        this.context = context;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;
        String email = null;

        if (request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("jwt-admin-token"))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        try {
            if (token != null) {
                email = jwtService.extractEmail(token);
            }   
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = context.getBean(AdminDetailsService.class).loadUserByUsername(email);
                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Add the token to the request attribute so controllers can access it
                    request.setAttribute("adminToken", token);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) { 
            // FIX: Catch general Exception to handle ALL JWT errors (not just ExpiredJwtException)
            
            // Token is invalid/expired. Clear the cookie and redirect to the login page.
            Cookie cookie = new Cookie("jwt-admin-token", null);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            
            // Redirect to login page with an 'invalid' parameter
            response.sendRedirect("/admin/login?invalid");
        }
    }
}
