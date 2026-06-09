package com.e_health_care.web.patient.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.model.PatientClinicalInfor;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientRecordServiceTest extends BaseServiceTest {

    @Mock
    private PatientClinicalInforRepository inforRepository;

    @InjectMocks
    private PatientRecordService service;

    @Test
    void getClinicalForEdit_shouldReturnDTO_whenFound() {
        PatientClinicalInfor info = new PatientClinicalInfor();
        info.setBloodType("A+");
        info.setAllergies("None");
        info.setChronicDiseases("None");
        info.setFamilyMedicalHistory("None");

        when(inforRepository.findByPatientId(1L)).thenReturn(Optional.of(info));

        PatientClinicalInforDTO result = service.getClinicalForEdit(1L);

        assertNotNull(result);
        assertEquals("A+", result.getBloodType());
    }

    @Test
    void getClinicalForEdit_shouldReturnEmptyDTO_whenNotFound() {
        when(inforRepository.findByPatientId(99L)).thenReturn(Optional.empty());

        PatientClinicalInforDTO result = service.getClinicalForEdit(99L);

        assertNotNull(result);
        assertEquals(99L, result.getId());
    }
}