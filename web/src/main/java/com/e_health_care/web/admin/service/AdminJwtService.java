package com.e_health_care.web.admin.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class AdminJwtService {
    @Value("${jwt.admin.secret}")
    private String SECRET;

    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        
        // FIX: Change 'ROLE_DOCTOR' to 'ROLE_ADMIN' to match validation
        claims.put("role", "ROLE_ADMIN"); 
        
        return Jwts.builder()
                    .claims(claims)
                    .subject(email)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) 
                    .signWith(getKey())
                    .compact();
    }

    // AdminJwtService.java

    private Key getKey() {
        // This Decoders.BASE64.decode() method now works with the new key provided above.
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
                && "ROLE_ADMIN".equals(role) 
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
