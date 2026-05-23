package com.e_health_care.web.api;

import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.service.PatientAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import com.e_health_care.web.patient.service.PatientUpdateProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.service.PatientRecordService;
@RestController
@RequestMapping("/api/patient")
public class PatientAuthApiController {

    @Autowired
    private PatientAuthenticationService authServicePatient;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody PatientDTO patientDTO) {
        String token = authServicePatient.verify(patientDTO);
        if (token != null) {
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody PatientDTO patientDTO) {
        authServicePatient.register(patientDTO);
        return ResponseEntity.ok(Map.of("message", "Registered successfully"));
    }

    @Autowired
    private PatientUpdateProfileService patientService;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        Patient patient = patientRepository.findByEmail(auth.getName())
                .orElse(null);
        if (patient == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody PatientDTO patientDTO) {
        try {
            patientService.updatePatient(patientDTO, id);
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @Autowired
    private PatientRecordService patientRecordService;

    @GetMapping("/clinical-info/{id}")
    public ResponseEntity<?> getClinicalInfo(@PathVariable Long id) {
        try {
            PatientClinicalInforDTO dto = patientRecordService.getClinicalForEdit(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}