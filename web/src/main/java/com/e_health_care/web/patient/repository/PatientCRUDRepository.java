package com.e_health_care.web.patient.repository;

import org.springframework.data.repository.CrudRepository;

import com.e_health_care.web.patient.model.Patient;

public interface PatientCRUDRepository extends CrudRepository<Patient, Long>{
    
}
