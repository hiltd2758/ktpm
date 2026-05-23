package com.e_health_care.web.api;

import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.doctor.service.DoctorViewPatientService;
import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.dto.PatientSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
public class ConsultationApiController {

    @Autowired
    private DoctorViewPatientService doctorViewPatientService;

    @Autowired
    private DoctorRepository doctorRepository;

    private Doctor getCurrentDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return doctorRepository.findByEmail(auth.getName());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        Doctor doctor = getCurrentDoctor();
        if (doctor == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        List<PatientSummaryDTO> patients = doctorViewPatientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<?> getPatientRecord(@PathVariable Long id) {
        Doctor doctor = getCurrentDoctor();
        if (doctor == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        PatientDTO patientDTO = doctorViewPatientService.getPatientProfile(id);
        if (patientDTO == null) return ResponseEntity.notFound().build();

        PatientClinicalInforDTO clinicalInforDTO = doctorViewPatientService.getPatientClinicalInfo(id);
        if (clinicalInforDTO == null) {
            clinicalInforDTO = new PatientClinicalInforDTO();
            clinicalInforDTO.setPatientId(id);
        }

        return ResponseEntity.ok(Map.of("patient", patientDTO, "clinicalInfo", clinicalInforDTO));
    }

    @PostMapping("/patient/{id}")
    public ResponseEntity<?> updatePatientRecord(@PathVariable Long id,
                                                 @RequestBody PatientClinicalInforDTO clinicalInforDTO) {
        Doctor doctor = getCurrentDoctor();
        if (doctor == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        doctorViewPatientService.updatePatientClinicalInfo(id, clinicalInforDTO, doctor.getEmail());
        return ResponseEntity.ok(Map.of("message", "Cập nhật bệnh án thành công"));
    }
}