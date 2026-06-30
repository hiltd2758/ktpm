package com.e_health_care.web.patient.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;

@Service
public class PatientAuthenticationService {
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientDetailsService patientDetailsService;

    @Autowired
    private PatientJwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Patient register(PatientDTO patientDTO) {
        if (patientRepository.findByEmail(patientDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already used, please try again.");
        }
        Patient patient = new Patient();
        patient.setEmail(patientDTO.getEmail());
        patient.setFirstName(patientDTO.getFirstName());
        patient.setLastName(patientDTO.getLastName());
        patient.setAddress(patientDTO.getAddress());
        patient.setPhone(String.valueOf(patientDTO.getPhone()));
        patient.setPassword(passwordEncoder.encode(patientDTO.getPassword()));
        return patientRepository.save(patient);
    }

    public String verify(PatientDTO patientDTO) {
        // 1. Kiểm tra Email rỗng hoặc null
        if (patientDTO.getEmail() == null || patientDTO.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        // 2. Kiểm tra định dạng Email (phải chứa @)
        if (!patientDTO.getEmail().contains("@")) {
            throw new RuntimeException("Invalid email format");
        }

        // 3. Kiểm tra độ dài Password (8-50 ký tự)
        if (patientDTO.getPassword() == null || patientDTO.getPassword().length() < 8 || patientDTO.getPassword().length() > 50) {
            throw new RuntimeException("Password must be 8-50 characters");
        }

        try {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider(patientDetailsService);
            provider.setPasswordEncoder(passwordEncoder);

            AuthenticationManager authManager = new ProviderManager(provider);

            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(patientDTO.getEmail(), patientDTO.getPassword()));

            return jwtService.generateToken(patientDTO.getEmail());

        } catch (BadCredentialsException e) {
            // If authentication fails, return null
            return null;
        }
    }
}
