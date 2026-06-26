package com.e_health_care.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Import các service của bạn
import com.e_health_care.web.doctor.service.DoctorDetailsService;

@Configuration
@Order(1)
@EnableWebSecurity
public class DoctorSecurityConfiguration {

    @Autowired
    private DoctorJwtFilter jwtFilter;

    @Autowired
    private DoctorDetailsService doctorDetailsService;

    // Cấu hình AuthenticationProvider để Spring biết dùng đúng DoctorDetailsService
    @Bean
    public AuthenticationProvider doctorAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(doctorDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain doctorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Đã đổi thành /api/doctor/** để khớp với đường dẫn thực tế của bạn trong Postman
                .securityMatcher("/api/doctor/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Gắn provider đã tạo ở trên vào filter chain
                .authenticationProvider(doctorAuthenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(request -> request
                        // Đường dẫn này cũng phải khớp với securityMatcher ở trên
                        .requestMatchers("/api/doctor/login").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}