package com.e_health_care.web.api;

import com.e_health_care.web.admin.dto.AdminDTO;
import com.e_health_care.web.admin.service.AdminAuthenticationService;
import com.e_health_care.web.admin.service.AdminManagementService;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.e_health_care.web.patient.model.Appointment;
import com.e_health_care.web.patient.service.PatientAppointmentService;
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private AdminAuthenticationService authService;

    @Autowired
    private AdminManagementService adminManagementService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminDTO adminDTO) {
        String token = authService.login(adminDTO);
        if (token != null) {
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    // --- DASHBOARD ---
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Patient> patients = patientRepository.findAll();
        return ResponseEntity.ok(Map.of("doctors", doctors, "patients", patients));
    }

    // --- DOCTOR MANAGEMENT ---
    @PostMapping("/doctor/delete")
    public ResponseEntity<?> deleteDoctor(
            @RequestBody Map<String, Long> body
    ) {

        try {

            Long id = body.get("id");

            adminManagementService.deleteDoctor(id);

            return ResponseEntity.ok(Map.of(
                    "message",
                    "Xóa doctor thành công"
            ));

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/doctor/update")
    public ResponseEntity<?> updateDoctorInfo(@RequestBody Doctor doctor) {
        try {
            Doctor existing = adminManagementService.getDoctorById(doctor.getId()).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            doctor.setPassword(existing.getPassword());
            doctor.setROLE(existing.getROLE());
            adminManagementService.updateDoctorInformation(doctor);
            return ResponseEntity.ok(Map.of("message", "Cập nhật doctor thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/doctor/change-password")
    public ResponseEntity<?> updateDoctorPassword(
            @RequestBody Map<String, String> body
    ) {

        Long id = Long.parseLong(
                body.get("id")
        );

        String newPassword = body.get("newPassword");

        if (adminManagementService.updateDoctorPassword(id, newPassword)) {

            return ResponseEntity.ok(Map.of(
                    "message",
                    "Đổi mật khẩu doctor thành công"
            ));
        }

        return ResponseEntity.badRequest().body(
                Map.of("error", "Doctor not found")
        );
    }

    // --- PATIENT MANAGEMENT ---
    @PostMapping("/patient/create")
    public ResponseEntity<?> createPatient(
            @RequestBody Patient patient
    ){

        patient.setPassword(
                passwordEncoder.encode(
                        patient.getPassword()
                )
        );

        patientRepository.save(patient);

        return ResponseEntity.ok(Map.of(
                "message",
                "Tạo patient thành công"
        ));
    }

    @PostMapping("/patient/delete")
    public ResponseEntity<?> deletePatient(
            @RequestBody Map<String, Long> body
    ) {

        try {

            Long id = body.get("id");

            adminManagementService.deletePatient(id);

            return ResponseEntity.ok(Map.of(
                    "message",
                    "Xóa patient thành công"
            ));

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/patient/update")
    public ResponseEntity<?> updatePatientInfo(@RequestBody Patient patient) {
        try {
            Patient existing = adminManagementService.getPatientById(patient.getId()).orElse(null);
            if (existing == null) return ResponseEntity.notFound().build();
            patient.setPassword(existing.getPassword());
            adminManagementService.updatePatientInformation(patient);
            return ResponseEntity.ok(Map.of("message", "Cập nhật patient thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/patient/change-password")
    public ResponseEntity<?> updatePatientPassword(
            @RequestBody Map<String, String> body
    ) {

        Long id = Long.parseLong(
                body.get("id")
        );

        String newPassword = body.get("newPassword");

        if (adminManagementService.updatePatientPassword(id, newPassword)) {

            return ResponseEntity.ok(Map.of(
                    "message",
                    "Đổi mật khẩu patient thành công"
            ));
        }

        return ResponseEntity.badRequest().body(
                Map.of("error", "Patient not found")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
    @Autowired
    private PatientAppointmentService appointmentService;

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        long totalDoctors = doctorRepository.count();
        long totalPatients = patientRepository.count();

        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        long pending = allAppointments.stream()
                .filter(a -> "PENDING".equals(a.getStatus())).count();
        long confirmed = allAppointments.stream()
                .filter(a -> "CONFIRMED".equals(a.getStatus())).count();
        long cancelled = allAppointments.stream()
                .filter(a -> "CANCELLED".equals(a.getStatus())).count();

        return ResponseEntity.ok(Map.of(
                "totalDoctors", totalDoctors,
                "totalPatients", totalPatients,
                "totalAppointments", allAppointments.size(),
                "pending", pending,
                "confirmed", confirmed,
                "cancelled", cancelled
        ));
    }
    @GetMapping("/generate-hash")
    public ResponseEntity<?> generateHash(@RequestParam String password) {
        return ResponseEntity.ok(Map.of("hash", passwordEncoder.encode(password)));
    }
}
