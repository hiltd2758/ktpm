package com.e_health_care.web.patient.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PatientClinicalInforDTO {
    private Long id; // add this
    private Long patientId; // optional
    private String bloodType;
    private String allergies;
    private String chronicDiseases;
    private String familyMedicalHistory;
    private Long lastUpdatedById; 
    private LocalDateTime lastUpDatedTime;
}
