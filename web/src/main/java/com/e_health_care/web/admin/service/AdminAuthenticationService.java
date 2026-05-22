package com.e_health_care.web.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.e_health_care.web.admin.dto.AdminDTO;

@Service
public class AdminAuthenticationService {

    @Autowired
    private AdminDetailsService adminDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired 
    private AdminJwtService jwtService;

    public String login(AdminDTO adminDTO) {
        try {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider(adminDetailsService);
            provider.setPasswordEncoder(passwordEncoder);

            AuthenticationManager authManager = new ProviderManager(provider);

            authManager.authenticate(new UsernamePasswordAuthenticationToken(adminDTO.getEmail(), adminDTO.getPassword()));

            return jwtService.generateToken(adminDTO.getEmail());
        } catch(BadCredentialsException e) {
            return null;
        }
    }
}
