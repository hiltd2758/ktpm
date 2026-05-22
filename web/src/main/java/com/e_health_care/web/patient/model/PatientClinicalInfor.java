package com.e_health_care.web.patient.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "patient_clinical_info")
public class PatientClinicalInfor {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", unique = true)
    private Patient patient;

    @Column(name = "blood_type", length = 5)
    private String bloodType;

    @Lob
    @Column(name = "allergies")
    private String allergies;

    @Lob
    @Column(name = "chronic_diseases")
    private String chronicDiseases;

    @Lob
    @Column(name = "family_medical_history")
    private String familyMedicalHistory;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpDatedTime;
}
