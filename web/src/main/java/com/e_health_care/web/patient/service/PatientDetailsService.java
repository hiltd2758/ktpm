package com.e_health_care.web.patient.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.model.PatientPrinciple;
import com.e_health_care.web.patient.repository.PatientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientDetailsService implements UserDetailsService{

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Patient patient = patientRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        if (patient == null) {
            throw new UsernameNotFoundException("The email is not found, please try again");
        }
        return new PatientPrinciple(patient);
    }
    
}