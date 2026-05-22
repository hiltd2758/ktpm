package com.e_health_care.web.doctor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.e_health_care.web.doctor.dto.DoctorDTO;

@Service
public class DoctorAuthenticationService {

    @Autowired
    private DoctorDetailsService doctorDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DoctorJwtService jwtService;
    
    public String verify(DoctorDTO doctorDTO) {
        try {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider(doctorDetailsService);
            provider.setPasswordEncoder(passwordEncoder);
            
            AuthenticationManager authManager = new ProviderManager(provider);

            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(doctorDTO.getEmail(), 
                                                        doctorDTO.getPassword(),
                                                        java.util.Collections.singletonList(new SimpleGrantedAuthority(doctorDTO.getUppercase_role()))
                    )
            );
            return jwtService.generateToken(doctorDTO.getEmail());
        } catch (BadCredentialsException e) {
            return null;
        }
    }
}
