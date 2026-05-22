package com.e_health_care.web.admin.service;

import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AdminManagementService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

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

    public void deletePatient(long id) {
        patientRepository.deleteById(id);
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