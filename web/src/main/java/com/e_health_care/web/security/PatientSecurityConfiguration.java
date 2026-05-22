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
@Order(2)
public class PatientSecurityConfiguration {

    @Autowired
    private PatientJwtFilter jwtFilterPatient;

    @Bean
    public SecurityFilterChain patientSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/patient/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilterPatient, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/patient/login", "/patient/register", "/patient/success", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}