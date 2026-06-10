package com.e_health_care.web.patient.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientServiceTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService service;

    @Test
    void getAllPatients_shouldReturnList() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("patient@test.com");
        when(patientRepository.findAll()).thenReturn(List.of(p));

        List<Patient> result = service.getAllPatients();

        assertEquals(1, result.size());
        assertEquals("patient@test.com", result.get(0).getEmail());
    }

    @Test
    void getAllPatients_shouldReturnEmptyList_whenNoPatientsExist() {
        when(patientRepository.findAll()).thenReturn(List.of());

        List<Patient> result = service.getAllPatients();

        assertEquals(0, result.size());
    }

    @Test
    void getPatientById_shouldReturnPatient_whenFound() {
        Patient p = new Patient();
        p.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));

        Optional<Patient> result = service.getPatientById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getPatientById_shouldReturnEmpty_whenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Patient> result = service.getPatientById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getPatientByEmail_shouldReturnPatient_whenFound() {
        Patient p = new Patient();
        p.setEmail("patient@test.com");
        when(patientRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(p));

        Optional<Patient> result = service.getPatientByEmail("patient@test.com");

        assertTrue(result.isPresent());
        assertEquals("patient@test.com", result.get().getEmail());
    }

    @Test
    void getPatientByEmail_shouldReturnEmpty_whenNotFound() {
        when(patientRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        Optional<Patient> result = service.getPatientByEmail("notfound@test.com");

        assertFalse(result.isPresent());
    }
}