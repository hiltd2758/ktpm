package com.e_health_care.web.admin.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import com.e_health_care.web.patient.repository.PatientRepository;

class AdminManagementServiceTest extends BaseServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientClinicalInforRepository patientClinicalInforRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Query nativeQuery;

    @InjectMocks
    private AdminManagementService adminManagementService;

    // --- Part 1: createPatient Tests ---

    @Test
    void createPatient_emailAlreadyExists_throwsRuntimeException() {
        // Arrange
        Patient patient = new Patient();
        patient.setEmail("existing@example.com");

        when(patientRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new Patient()));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            adminManagementService.createPatient(patient);
        });

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void createPatient_newEmail_callsSave() {
        // Arrange
        Patient patient = new Patient();
        patient.setEmail("new@example.com");

        when(patientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(patientRepository.save(patient)).thenReturn(patient);

        // Act
        Patient result = adminManagementService.createPatient(patient);

        // Assert
        assertNotNull(result);
        verify(patientRepository, times(1)).save(patient);
    }

    // --- Part 2: updatePatient Tests ---

    @Test
    void updatePatient_patientIdDoesNotExist_throwsRuntimeException() {
        // Arrange
        Patient patient = new Patient();
        patient.setId(99L);

        when(patientRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            adminManagementService.updatePatient(patient);
        });

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void updatePatient_patientIdExists_callsSave() {
        // Arrange
        Patient patient = new Patient();
        patient.setId(1L);

        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.save(patient)).thenReturn(patient);

        // Act
        Patient result = adminManagementService.updatePatient(patient);

        // Assert
        assertNotNull(result);
        verify(patientRepository, times(1)).save(patient);
    }

    // --- Part 3: deletePatient Tests ---

    @Test
    void deletePatient_patientIdDoesNotExist_throwsRuntimeException() {
        // Arrange
        long id = 99L;

        when(patientRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            adminManagementService.deletePatient(id);
        });

        verify(patientRepository, never()).deleteById(anyLong());
    }

    @Test
    void deletePatient_patientIdExists_callsDelete() {
        // Arrange
        long id = 1L;

        when(patientRepository.existsById(id)).thenReturn(true);
        when(patientClinicalInforRepository.findByPatientId(id)).thenReturn(Optional.empty());
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);

        // Act
        adminManagementService.deletePatient(id);

        // Assert
        verify(patientRepository, times(1)).deleteById(id);
        verify(entityManager, times(1)).flush();
    }
}
