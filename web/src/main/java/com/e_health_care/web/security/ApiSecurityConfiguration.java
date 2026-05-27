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

@Configuration
@EnableWebSecurity
@Order(4)
public class ApiSecurityConfiguration {

    @Autowired
    private PatientJwtFilter patientJwtFilter;

    @Autowired
    private DoctorJwtFilter doctorJwtFilter;

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/patient/login", "/api/patient/register",
                                "/api/doctor/login", "/api/admin/login",
                                "/api/admin/generate-hash").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(patientJwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(doctorJwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}