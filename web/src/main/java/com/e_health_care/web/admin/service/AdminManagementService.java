package com.e_health_care.web.admin.service;

import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.Optional;

@Service
@Transactional
public class AdminManagementService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientClinicalInforRepository patientClinicalInforRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder; // Used for secure password hashing

    // --- DOCTOR MANAGEMENT ---

    public Optional<Doctor> getDoctorById(long id) {
        return doctorRepository.findById(id);
    }

    public void deleteDoctor(long id) {
        doctorRepository.deleteById(id);
    }

    // Update doctor's information (but not password)
    public Doctor updateDoctorInformation(Doctor updatedDoctor) {
        return doctorRepository.save(updatedDoctor);
    }

    // Update doctor's password
    public boolean updateDoctorPassword(long id, String newPassword) {
        Optional<Doctor> optionalDoctor = doctorRepository.findById(id);
        if (optionalDoctor.isPresent()) {
            Doctor doctor = optionalDoctor.get();
            // Encode the new password before saving
            doctor.setPassword(passwordEncoder.encode(newPassword));
            doctorRepository.save(doctor);
            return true;
        }
        return false;
    }

    // --- PATIENT MANAGEMENT ---

    public Optional<Patient> getPatientById(long id) {
        return patientRepository.findById(id);
    }

    public Patient createPatient(Patient patient) {
        if (patientRepository.findByEmail(patient.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + patient.getEmail());
        }
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Patient patient) {
        if (patient == null || patient.getId() == null || !patientRepository.existsById(patient.getId())) {
            throw new RuntimeException("Patient not found");
        }
        return patientRepository.save(patient);
    }

    public void deletePatient(long id) {
        if (!patientRepository.existsById(id)) {
            throw new RuntimeException("Patient not found with ID: " + id);
        }

        // First delete dependent clinical info records
        patientClinicalInforRepository.findByPatientId(id).ifPresent(clinicalInfo -> {
            patientClinicalInforRepository.delete(clinicalInfo);
        });

        // Delete consultations using native query (since no entity mapped)
        Query deleteConsultationsQuery = entityManager.createNativeQuery(
            "DELETE FROM consultation WHERE patient_id = ?1"
        );
        deleteConsultationsQuery.setParameter(1, id);
        deleteConsultationsQuery.executeUpdate();

        // Finally delete patient
        patientRepository.deleteById(id);
        
        // Flush to ensure all operations are committed
        entityManager.flush();
    }

    // Update patient's information (but not password)
    public Patient updatePatientInformation(Patient updatedPatient) {
        return patientRepository.save(updatedPatient);
    }

    // Update patient's password
    public boolean updatePatientPassword(long id, String newPassword) {
        Optional<Patient> optionalPatient = patientRepository.findById(id);
        if (optionalPatient.isPresent()) {
            Patient patient = optionalPatient.get();
            // Encode the new password before saving
            patient.setPassword(passwordEncoder.encode(newPassword));
            patientRepository.save(patient);
            return true;
        }
        return false;
    }
}