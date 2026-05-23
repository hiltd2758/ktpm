package com.e_health_care.web.api;

import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.doctor.service.DoctorAuthenticationService;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.service.PatientAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.service.DoctorService;
@RestController
@RequestMapping("/api/doctor")
public class DoctorApiController {

    @Autowired
    private DoctorAuthenticationService authService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientAppointmentService patientAppointmentService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody DoctorDTO doctorDTO) {
        String token = authService.verify(doctorDTO);
        if (token != null) {
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    @GetMapping("/appointment/list")
    public ResponseEntity<?> getAppointments() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Doctor doctor = doctorRepository.findByEmail(auth.getName());
        if (doctor == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        List<Appointment> appointments = patientAppointmentService.getAppointmentsByDoctor(doctor.getId());
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/appointment/update/{id}/{status}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            patientAppointmentService.updateAppointmentStatus(id, status);
            return ResponseEntity.ok(Map.of("message", "Cập nhật thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
    @Autowired
    private DoctorService doctorService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        DoctorDTO doctor = doctorService.getDoctorByEmail(auth.getName());
        return ResponseEntity.ok(doctor);
    }

    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody DoctorDTO doctorDTO) {
        try {
            doctorService.updateDoctorProfile(doctorDTO);
            return ResponseEntity.ok(Map.of("message", "Cập nhật hồ sơ thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}