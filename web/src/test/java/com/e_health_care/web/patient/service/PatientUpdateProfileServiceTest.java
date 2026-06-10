package com.e_health_care.web.patient.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientUpdateProfileServiceTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientUpdateProfileService service;

    @Test
    void getPatientById_shouldReturnDTO_whenFound() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("patient@test.com");
        p.setFirstName("John");
        p.setLastName("Doe");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));

        PatientDTO result = service.getPatientById(1L);

        assertNotNull(result);
        assertEquals("patient@test.com", result.getEmail());
        assertEquals("John", result.getFirstName());
    }

    @Test
    void getPatientById_shouldThrow_whenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getPatientById(99L));
    }

    @Test
    void updatePatient_shouldSave_whenFound() {
        Patient p = new Patient();
        p.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));

        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPhone("0909123456");

        service.updatePatient(dto, 1L);

        verify(patientRepository, times(1)).save(any());
    }

    @Test
    void updatePatient_shouldThrow_whenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.updatePatient(new PatientDTO(), 99L)
        );
    }
}