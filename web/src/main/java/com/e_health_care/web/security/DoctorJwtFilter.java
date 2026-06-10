package com.e_health_care.web.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.e_health_care.web.doctor.service.DoctorDetailsService;
import com.e_health_care.web.doctor.service.DoctorJwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DoctorJwtFilter extends OncePerRequestFilter {

    @Autowired
    private DoctorJwtService jwtService;

    @Autowired
    private ApplicationContext context;

    // Các endpoint không cần token
    private static final String[] PUBLIC_PATHS = {
            "/api/doctor/login",
            "/api/patient/login",
            "/api/patient/register",
            "/api/admin/login"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Bỏ qua nếu không phải /api/doctor
        if (!path.startsWith("/api/doctor")) {
            return true;
        }

        // Bỏ qua public paths
        for (String publicPath : PUBLIC_PATHS) {
            if (path.equals(publicPath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // Ưu tiên Authorization Header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // Fallback Cookie
        if (token == null && request.getCookies() != null) {
            token = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("jwt-doctor-token"))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        // Không có token -> cho đi tiếp (Security config sẽ xử lý 401)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String role = jwtService.extractRoleWithoutVerification(token);

            // Token không phải của doctor -> cho đi tiếp
            if (!"ROLE_DOCTOR".equals(role)) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractEmail(token);

            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = context
                        .getBean(DoctorDetailsService.class)
                        .loadUserByUsername(username);

                if (jwtService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("DOCTOR AUTH SUCCESS: " + username);
                } else {
                    System.out.println("TOKEN INVALID for: " + username);
                    sendUnauthorized(response, "Invalid token");
                    return;
                }
            }

        } catch (ExpiredJwtException e) {
            sendUnauthorized(response, "Token expired");
            return;

        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            sendUnauthorized(response, "Invalid token format");
            return;

        } catch (UsernameNotFoundException e) {
            // User trong token không còn tồn tại trong DB -> cho đi tiếp, Security xử lý
            filterChain.doFilter(request, response);
            return;

        } catch (Exception e) {
            e.printStackTrace();
            sendUnauthorized(response, "Authentication error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}

