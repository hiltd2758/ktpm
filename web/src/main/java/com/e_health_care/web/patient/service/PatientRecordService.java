package com.e_health_care.web.patient.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;

@Service
public class PatientRecordService {

    @Autowired
    private PatientClinicalInforRepository inforRepository;

    public PatientClinicalInforDTO getClinicalForEdit(Long patientId) {
        return inforRepository.findByPatientId(patientId)
            .map(info -> {
                PatientClinicalInforDTO dto = new PatientClinicalInforDTO();
                dto.setBloodType(info.getBloodType());
                dto.setAllergies(info.getAllergies());
                dto.setChronicDiseases(info.getChronicDiseases());
                dto.setFamilyMedicalHistory(info.getFamilyMedicalHistory());
                dto.setLastUpDatedTime(LocalDateTime.now());
                return dto;
            })
            .orElseGet(() -> {
                PatientClinicalInforDTO dto = new PatientClinicalInforDTO();
                dto.setId(patientId);
                return dto;
            }); 
    }
}
