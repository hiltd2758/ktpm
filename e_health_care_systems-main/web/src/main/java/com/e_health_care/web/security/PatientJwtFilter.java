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

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract email from token
        userEmail = jwtServicePatient.extractEmail(jwt);

        // 3. Validate token and set security context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.patientDetailsService.loadUserByUsername(userEmail);

            if (jwtServicePatient.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Add the token to the request attribute so controllers can access it
                request.setAttribute("patientToken", jwt);
            }
        }
        filterChain.doFilter(request, response);
    }
}
