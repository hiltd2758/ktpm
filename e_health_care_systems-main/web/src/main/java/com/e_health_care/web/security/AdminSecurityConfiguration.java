package com.e_health_care.web.security;

import java.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.e_health_care.web.admin.service.AdminJwtService;

@Configuration
@Order(3)
@EnableWebSecurity
public class AdminSecurityConfiguration {

    @Autowired
    private final AdminJwtService jwtService;
    @Autowired
    private final ApplicationContext context;
    
    public AdminSecurityConfiguration(AdminJwtService jwtService, ApplicationContext context) {
        this.jwtService = jwtService;
        this.context = context;
    }
    
    @Bean
    public AdminJwtFilter adminJwtFilter() {
        return new AdminJwtFilter(jwtService, context);
    }

    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
            )
            // FIX 2: Add exception handling to redirect unauthenticated users
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, authException) -> 
                    response.sendRedirect("/admin/login?unauthorized")
                )
            )
            // FIX 3: Use the filter bean creator method
            .addFilterBefore(adminJwtFilter(), UsernamePasswordAuthenticationFilter.class); 
            
        return http.build();
    }
}
