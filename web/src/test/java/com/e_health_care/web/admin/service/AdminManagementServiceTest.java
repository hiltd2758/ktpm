package com.e_health_care.web.admin.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.patient.model.PatientClinicalInfor;

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

    @Test
    void getDoctorById_callsRepository() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(new Doctor()));
        assertTrue(adminManagementService.getDoctorById(1L).isPresent());
        verify(doctorRepository).findById(1L);
    }

    @Test
    void deleteDoctor_callsDeleteById() {
        adminManagementService.deleteDoctor(1L);
        verify(doctorRepository).deleteById(1L);
    }

    @Test
    void updateDoctorInformation_callsSave() {
        Doctor doctor = new Doctor();
        when(doctorRepository.save(doctor)).thenReturn(doctor);
        assertNotNull(adminManagementService.updateDoctorInformation(doctor));
        verify(doctorRepository).save(doctor);
    }

    @Test
    void updateDoctorPassword_doctorExists_updatesAndReturnsTrue() {
        Doctor doctor = new Doctor();
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedPass");

        boolean result = adminManagementService.updateDoctorPassword(1L, "newPass");

        assertTrue(result);
        assertEquals("encodedPass", doctor.getPassword());
        verify(doctorRepository).save(doctor);
    }

    @Test
    void updateDoctorPassword_doctorDoesNotExist_returnsFalse() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
        boolean result = adminManagementService.updateDoctorPassword(1L, "newPass");
        assertFalse(result);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void getPatientById_callsRepository() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(new Patient()));
        assertTrue(adminManagementService.getPatientById(1L).isPresent());
        verify(patientRepository).findById(1L);
    }

    @Test
    void updatePatient_patientIsNull_throwsException() {
        assertThrows(RuntimeException.class, () -> adminManagementService.updatePatient(null));
    }

    @Test
    void updatePatient_patientIdIsNull_throwsException() {
        Patient patient = new Patient(); // ID is null by default
        assertThrows(RuntimeException.class, () -> adminManagementService.updatePatient(patient));
    }

    @Test
    void deletePatient_withClinicalInfo_deletesDependencies() {
        long id = 1L;
        when(patientRepository.existsById(id)).thenReturn(true);

        PatientClinicalInfor clinicalInfo = new PatientClinicalInfor();
        when(patientClinicalInforRepository.findByPatientId(id)).thenReturn(Optional.of(clinicalInfo));
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);

        adminManagementService.deletePatient(id);

        verify(patientClinicalInforRepository).delete(clinicalInfo);
        verify(nativeQuery).setParameter(1, id);
        verify(nativeQuery).executeUpdate();
        verify(patientRepository).deleteById(id);
        verify(entityManager).flush();
    }

    @Test
    void updatePatientInformation_callsSave() {
        Patient patient = new Patient();
        when(patientRepository.save(patient)).thenReturn(patient);
        assertNotNull(adminManagementService.updatePatientInformation(patient));
        verify(patientRepository).save(patient);
    }

    @Test
    void updatePatientPassword_patientExists_updatesAndReturnsTrue() {
        Patient patient = new Patient();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedPass");

        boolean result = adminManagementService.updatePatientPassword(1L, "newPass");

        assertTrue(result);
        assertEquals("encodedPass", patient.getPassword());
        verify(patientRepository).save(patient);
    }

    @Test
    void updatePatientPassword_patientDoesNotExist_returnsFalse() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());
        boolean result = adminManagementService.updatePatientPassword(1L, "newPass");
        assertFalse(result);
        verify(patientRepository, never()).save(any());
    }
}
