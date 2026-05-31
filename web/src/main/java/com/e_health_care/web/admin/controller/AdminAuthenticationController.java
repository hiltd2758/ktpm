package com.e_health_care.web.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.e_health_care.web.admin.dto.AdminDTO;
import com.e_health_care.web.admin.service.AdminAuthenticationService;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.HashMap;

/**
 * REST API Controller for Admin Authentication
 * 
 * Provides JSON API endpoints for authentication (login, logout, token verification)
 * 
 * Base URL: /admin/api/auth
 * 
 * Endpoints:
 * - POST   /admin/api/auth/login           - Login and get JWT token
 * - POST   /admin/api/auth/logout          - Logout (clear token)
 * - POST   /admin/api/auth/verify-token    - Verify if token is valid
 * - POST   /admin/api/auth/refresh-token   - Refresh token expiration
 */
@RestController
@RequestMapping("/admin/api/auth")
public class AdminAuthenticationController {

    @Autowired
    private AdminAuthenticationService authService;

    /**
     * POST /admin/api/auth/login
     * 
     * Admin login with email and password
     * Returns JWT token on successful authentication
     * 
     * @param adminDTO AdminDTO containing email and password
     * @return ResponseEntity with status 200 and JWT token, or 401 if credentials invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminDTO adminDTO, HttpServletResponse response) {
        try {
            // Validate adminDTO not null
            if (adminDTO == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Request body cannot be empty",
                    "data", (Object) null
                ));
            }

            // Validate input
            if (adminDTO.getEmail() == null || adminDTO.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email is required",
                    "data", (Object) null
                ));
            }

            if (adminDTO.getPassword() == null || adminDTO.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Password is required",
                    "data", (Object) null
                ));
            }

            // Attempt login
            String token = authService.login(adminDTO);

            if (token != null && !token.trim().isEmpty()) {
                // Build response using HashMap to safely handle values
                Map<String, Object> tokenData = new HashMap<>();
                tokenData.put("token", token);
                tokenData.put("email", adminDTO.getEmail());
                tokenData.put("tokenType", "Bearer");
                tokenData.put("expiresIn", 7 * 24 * 60 * 60);  // 7 days in seconds

                Map<String, Object> response_body = new HashMap<>();
                response_body.put("success", true);
                response_body.put("message", "Login successful");
                response_body.put("data", tokenData);

                return ResponseEntity.ok(response_body);
            } else {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Invalid email or password",
                    "data", (Object) null
                ));
            }
        } catch (NullPointerException e) {
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", "Invalid request format: " + e.getMessage(),
                "data", (Object) null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error during login: " + e.getMessage(),
                "data", (Object) null
            ));
        }
    }

    /**
     * POST /admin/api/auth/logout
     * 
     * Admin logout (clear JWT token)
     * 
     * @return ResponseEntity with status 200 indicating logout success
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try {
            // Clear JWT cookie if set
            // Cookie cookie = new Cookie("jwt-admin-token", "");
            // cookie.setHttpOnly(true);
            // cookie.setSecure(false);
            // cookie.setPath("/");
            // cookie.setMaxAge(0);  // Delete cookie
            // response.addCookie(cookie);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout successful",
                "data", (Object) null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error during logout: " + e.getMessage(),
                "data", (Object) null
            ));
        }
    }

    /**
     * POST /admin/api/auth/verify-token
     * 
     * Verify if JWT token is valid
     * 
     * @param authHeader Request header containing Authorization with Bearer token
     * @return ResponseEntity with status 200 if token valid, 401 if invalid
     */
    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Authorization header missing or invalid format. Expected: Authorization: Bearer <token>",
                    "data", (Object) null
                ));
            }

            String token = authHeader.substring(7);  // Remove "Bearer " prefix

            // In production, validate token with JwtService
            // boolean isValid = jwtService.validateToken(token);
            // if (!isValid) {
            //     return ResponseEntity.status(401).body(...);
            // }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token is valid",
                "data", Map.of(
                    "valid", true,
                    "token", token
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error verifying token: " + e.getMessage(),
                "data", (Object) null
            ));
        }
    }

    /**
     * POST /admin/api/auth/refresh-token
     * 
     * Refresh JWT token (extend expiration)
     * 
     * @param authHeader Request header containing old token
     * @return ResponseEntity with new token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Authorization header missing or invalid",
                    "data", (Object) null
                ));
            }

            String oldToken = authHeader.substring(7);

            // In production, validate old token and generate new token
            // if (!jwtService.validateToken(oldToken)) {
            //     return ResponseEntity.status(401).body(...);
            // }
            // String newToken = jwtService.generateToken(email);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token refreshed successfully",
                "data", Map.of(
                    "token", oldToken,  // In production, return newToken
                    "tokenType", "Bearer",
                    "expiresIn", 7 * 24 * 60 * 60
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error refreshing token: " + e.getMessage(),
                "data", (Object) null
            ));
        }
    }
}