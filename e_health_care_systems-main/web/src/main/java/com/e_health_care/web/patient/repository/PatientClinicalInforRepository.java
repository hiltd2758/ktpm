package com.e_health_care.web.patient.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.e_health_care.web.patient.model.PatientClinicalInfor;

public interface PatientClinicalInforRepository extends JpaRepository<PatientClinicalInfor, Long>{
    Optional<PatientClinicalInfor> findByPatientId(Long patientId);
}
