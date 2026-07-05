package com.e_health_care.web.api;

import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.service.PatientAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import com.e_health_care.web.patient.service.PatientUpdateProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.service.PatientRecordService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patient")
public class PatientAuthApiController {

    @Autowired
    private PatientAuthenticationService authServicePatient;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody PatientDTO patientDTO, HttpServletResponse response) {
        String token = authServicePatient.verify(patientDTO);
        if (token != null) {
            response.setHeader("Set-Cookie",
                    "jwt-patient-token=" + token +
                            "; Path=/; HttpOnly; Max-Age=86400; SameSite=Lax");
            return ResponseEntity.ok(Map.of("message", "Login successful", "token", token));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody PatientDTO patientDTO) {
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
        if (!(auth instanceof UsernamePasswordAuthenticationToken)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Patient patient = patientRepository.findByEmail(auth.getName()).orElse(null);
        if (patient == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody PatientDTO patientDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Verify id thuộc về patient đang login
        Patient current = patientRepository.findByEmail(auth.getName()).orElse(null);
        if (current == null || !current.getId().equals(id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        try {
            patientService.updatePatient(patientDTO, id);
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        Cookie cookie = new Cookie("jwt-patient-token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

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