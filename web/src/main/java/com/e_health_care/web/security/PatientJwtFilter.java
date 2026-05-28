package com.e_health_care.web.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.e_health_care.web.patient.service.PatientJwtService;
import com.e_health_care.web.patient.service.PatientDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PatientJwtFilter extends OncePerRequestFilter {

    @Autowired
    private PatientJwtService jwtServicePatient;

    @Autowired
    private PatientDetailsService patientDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String jwt = null;
        String userEmail = null;

        // 1. Extract token from cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt-patient-token".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                }
            }
        }

        // 2. Fallback: đọc từ Authorization header (cho Postman)
        if (jwt == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract email from token
            try {
                userEmail = jwtServicePatient.extractEmail(jwt);
            } catch (Exception e) {
                filterChain.doFilter(request, response);
                return;
            }

        // 4. Validate token and set security context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.patientDetailsService.loadUserByUsername(userEmail);

            if (jwtServicePatient.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                request.setAttribute("patientToken", jwt);
            }
        }
        filterChain.doFilter(request, response);
    }
}