package com.e_health_care.web;

import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.doctor.service.DoctorViewPatientService;
import com.e_health_care.web.patient.dto.PatientSummaryDTO;
import com.e_health_care.web.doctor.dto.DoctorSummaryDTO; // Import DoctorSummaryDTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors; // Import Collectors

@RestController
@RequestMapping("/api")
public class UserAPIController {

    @Autowired
    private DoctorViewPatientService doctorViewPatientService;

    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping("/patients")
    public List<PatientSummaryDTO> getAllPatients() {
        return doctorViewPatientService.getAllPatients();
    }

    @GetMapping("/doctors")
    public List<DoctorSummaryDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(doctor -> {
                    DoctorSummaryDTO dto = new DoctorSummaryDTO();
                    dto.setId(doctor.getId());
                    dto.setEmail(doctor.getEmail());
                    dto.setFullName(doctor.getFirstName() + " " + doctor.getLastName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
