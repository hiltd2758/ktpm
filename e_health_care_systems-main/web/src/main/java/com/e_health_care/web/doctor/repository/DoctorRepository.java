package com.e_health_care.web.doctor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.e_health_care.web.doctor.model.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Doctor findByEmail(String email);
    Optional<Doctor> findById(long id);
}
