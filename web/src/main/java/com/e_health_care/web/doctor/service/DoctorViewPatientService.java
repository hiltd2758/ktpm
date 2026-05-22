package com.e_health_care.web.doctor.service;

import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.model.PatientClinicalInfor;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.e_health_care.web.patient.dto.PatientSummaryDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorViewPatientService {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PatientClinicalInforRepository patientClinicalInforRepository;
    @Autowired
    private DoctorRepository doctorRepository;

    public List<PatientSummaryDTO> getAllPatients() {
        return patientRepository.findAll()
            .stream()
            .map(p -> {
                PatientSummaryDTO dto = new PatientSummaryDTO();
                dto.setId(p.getId());
                dto.setEmail(p.getEmail());
                dto.setFullName(p.getFirstName() + " " + p.getLastName());
                dto.setFirstName(p.getFirstName());
                dto.setLastName(p.getLastName());
                dto.setPhone(p.getPhone());
                dto.setAddress(p.getAddress());
                dto.setDateOfBirth(p.getDateOfBirth());
                dto.setAvatar(p.getAvatar());
                return dto;
            })
            .toList();
    }

    public PatientClinicalInforDTO getPatientClinicalInfo(Long patientId) {
        Optional<PatientClinicalInfor> clinicalInforOpt = patientClinicalInforRepository.findByPatientId(patientId);
        if (clinicalInforOpt.isPresent()) {
            PatientClinicalInfor clinicalInfor = clinicalInforOpt.get();
            PatientClinicalInforDTO dto = new PatientClinicalInforDTO();
            dto.setId(clinicalInfor.getId());
            dto.setPatientId(clinicalInfor.getPatient().getId());
            dto.setBloodType(clinicalInfor.getBloodType());
            dto.setAllergies(clinicalInfor.getAllergies());
            dto.setChronicDiseases(clinicalInfor.getChronicDiseases());
            dto.setFamilyMedicalHistory(clinicalInfor.getFamilyMedicalHistory());
            dto.setLastUpDatedTime(clinicalInfor.getLastUpDatedTime());
            return dto;
        }
        return null;
    }

    public PatientDTO getPatientProfile(Long patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            PatientDTO dto = new PatientDTO();
            dto.setId(patient.getId());
            dto.setEmail(patient.getEmail());
            dto.setFirstName(patient.getFirstName());
            dto.setLastName(patient.getLastName());
            dto.setAddress(patient.getAddress());
            dto.setPhone(patient.getPhone());
            dto.setDateOfBirth(patient.getDateOfBirth());
            dto.setAvatar(patient.getAvatar());
            return dto;
        }
        return null;
    }

    public void updatePatientClinicalInfo(Long patientId, PatientClinicalInforDTO clinicalInforDTO, String doctorEmail) {
        Doctor doctor = doctorRepository.findByEmail(doctorEmail);
        if (doctor == null) {
            throw new UsernameNotFoundException("Doctor not found with email: " + doctorEmail);
        }

        PatientClinicalInfor clinicalInfor = patientClinicalInforRepository.findByPatientId(patientId)
                .orElse(new PatientClinicalInfor());

        clinicalInfor.setBloodType(clinicalInforDTO.getBloodType());
        clinicalInfor.setAllergies(clinicalInforDTO.getAllergies());
        clinicalInfor.setChronicDiseases(clinicalInforDTO.getChronicDiseases());
        clinicalInfor.setFamilyMedicalHistory(clinicalInforDTO.getFamilyMedicalHistory());
        clinicalInfor.setLastUpDatedTime(LocalDateTime.now());

        if (clinicalInfor.getPatient() == null) {
            patientRepository.findById(patientId).ifPresent(clinicalInfor::setPatient);
        }

        patientClinicalInforRepository.save(clinicalInfor);
    }
}
