package com.e_health_care.web.doctor.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.model.PatientClinicalInfor;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
@MockitoSettings(strictness = Strictness.LENIENT)
class DoctorViewPatientServiceTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientClinicalInforRepository patientClinicalInforRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorViewPatientService service;

    @Test
    void getAllPatients_shouldReturnList() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("patient@test.com");
        p.setFirstName("John");
        p.setLastName("Doe");

        when(patientRepository.findAll()).thenReturn(List.of(p));

        var result = service.getAllPatients();

        assertEquals(1, result.size());
        assertEquals("patient@test.com", result.get(0).getEmail());
    }
    @Test
    void getAllPatients_shouldReturnEmptyList_whenNoPatientsExist() {
        when(patientRepository.findAll()).thenReturn(List.of());
        var result = service.getAllPatients();
        assertEquals(0, result.size());
    }
    @Test
    void getPatientProfile_shouldReturnDTO_whenFound() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("patient@test.com");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));

        PatientDTO result = service.getPatientProfile(1L);

        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    void getPatientProfile_shouldReturnNull_whenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        PatientDTO result = service.getPatientProfile(99L);

        assertNull(result);
    }

    @Test
    void getPatientClinicalInfo_shouldReturnDTO_whenFound() {
        PatientClinicalInfor info = new PatientClinicalInfor();
        info.setId(1L);
        info.setBloodType("B+");

        Patient p = new Patient();
        p.setId(1L);
        info.setPatient(p);

        when(patientClinicalInforRepository.findByPatientId(1L)).thenReturn(Optional.of(info));

        PatientClinicalInforDTO result = service.getPatientClinicalInfo(1L);

        assertNotNull(result);
        assertEquals("B+", result.getBloodType());
    }

    @Test
    void getPatientClinicalInfo_shouldReturnNull_whenNotFound() {
        when(patientClinicalInforRepository.findByPatientId(99L)).thenReturn(Optional.empty());

        PatientClinicalInforDTO result = service.getPatientClinicalInfo(99L);

        assertNull(result);
    }

    @Test
    void updatePatientClinicalInfo_shouldThrow_whenDoctorNotFound() {
        when(doctorRepository.findByEmail("notfound@test.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () ->
                service.updatePatientClinicalInfo(1L, new com.e_health_care.web.patient.dto.PatientClinicalInforDTO(), "notfound@test.com")
        );
    }

    @Test
    void updatePatientClinicalInfo_shouldThrow_whenPatientNotFound() {
        Doctor doctor = new Doctor();
        when(doctorRepository.findByEmail("doctor@test.com")).thenReturn(doctor);
        when(patientClinicalInforRepository.findByPatientId(99L)).thenReturn(Optional.empty());
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.updatePatientClinicalInfo(99L, new com.e_health_care.web.patient.dto.PatientClinicalInforDTO(), "doctor@test.com")
        );
    }

    @Test
    void updatePatientClinicalInfo_shouldSave_whenValid() {
        Doctor doctor = new Doctor();
        when(doctorRepository.findByEmail("doctor@test.com")).thenReturn(doctor);

        PatientClinicalInfor existing = new PatientClinicalInfor();
        Patient p = new Patient();
        p.setId(1L);
        existing.setPatient(p);

        when(patientClinicalInforRepository.findByPatientId(1L)).thenReturn(Optional.of(existing));

        service.updatePatientClinicalInfo(1L, new com.e_health_care.web.patient.dto.PatientClinicalInforDTO(), "doctor@test.com");

        verify(patientClinicalInforRepository, times(1)).save(any());
    }
}