package com.e_health_care.web.doctor.repository;

import org.springframework.data.repository.CrudRepository;

import com.e_health_care.web.doctor.model.Doctor;

public interface DoctorCRUDRepository extends CrudRepository<Doctor, Long> {
    
}
