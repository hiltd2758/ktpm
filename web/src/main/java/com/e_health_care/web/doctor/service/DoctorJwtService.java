package com.e_health_care.web.doctor.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class DoctorJwtService {

    // Use a static, stable secret key. This should ideally be in application.properties.
    // This key MUST be long and secure for a production environment.
    @Value("${jwt.doctor.secret}")
    private String SECRET;

    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        // Add the role claim for the doctor
        claims.put("role", "ROLE_DOCTOR");
        return Jwts.builder()
                    .claims(claims)
                    .subject(email)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 hours
                    .signWith(getKey())
                    .compact();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmail(String token) {
        // Extract the subject (email) from the JWT token
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        // Extract the role claim from the JWT token
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    @Deprecated
    private Claims extractAllClaims(String token) {
        // Use the parser compatible with the project's jjwt version
        return Jwts.parser()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        final String role = extractRole(token);
        // Check if email matches, token is not expired, AND the role is correct
        return email.equals(userDetails.getUsername()) 
                && "ROLE_DOCTOR".equals(role) 
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
