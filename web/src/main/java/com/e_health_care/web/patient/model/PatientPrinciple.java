package com.e_health_care.web.patient.model;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class PatientPrinciple implements UserDetails {
    
    private Patient patient;

    public PatientPrinciple(Patient patient) {
        this.patient = patient;
    }

    // Getter cho patient để controller truy cập (tránh NPE)
    public Patient getPatient() {
        return patient;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("PATIENT"));
    }

    @Override
    public String getPassword() {
        return patient.getPassword();
    }

    @Override
    public String getUsername() {
        return patient.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Production: return patient.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Production: return !patient.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Production: return patient.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return true;  // Production: return patient.isEnabled();
    }

    // Optional: Override cho cache/session an toàn
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PatientPrinciple that = (PatientPrinciple) obj;
        // return patient != null ? patient.getId() == that.patient.getId() : that.patient == null;
        return patient != null ? patient.getId() == that.patient.getId() : that.patient == null;
    }

    @Override
    public int hashCode() {
        return patient != null ? Long.hashCode(patient.getId()) : 0;
    }
}