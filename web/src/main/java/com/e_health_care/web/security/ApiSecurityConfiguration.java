package com.e_health_care.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@Order(4)
public class ApiSecurityConfiguration {

    @Autowired
    private PatientJwtFilter patientJwtFilter;

    @Autowired
    private DoctorJwtFilter doctorJwtFilter;

    @Autowired
    private AdminJwtFilter adminJwtFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        config.setAllowCredentials(true); // bắt buộc khi dùng cookie
        config.setMaxAge(3600L);          // cache preflight 1 giờ

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/patient/login",
                                "/api/patient/register",
                                "/api/doctor/login",
                                "/api/admin/login",
                                "/api/admin/generate-hash"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(doctorJwtFilter, UsernamePasswordAuthenticationFilter.class)
                //.addFilterBefore(adminJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(patientJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
