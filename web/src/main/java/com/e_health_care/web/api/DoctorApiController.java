    package com.e_health_care.web.api;

    import com.e_health_care.web.doctor.dto.DoctorDTO;
    import com.e_health_care.web.doctor.model.Doctor;
    import com.e_health_care.web.doctor.repository.DoctorRepository;
    import com.e_health_care.web.doctor.service.DoctorAuthenticationService;
    import com.e_health_care.web.doctor.service.DoctorViewPatientService;
    import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
    import com.e_health_care.web.patient.dto.PatientDTO;
    import com.e_health_care.web.patient.dto.PatientSummaryDTO;
    import com.e_health_care.web.patient.model.Appointment;
    import com.e_health_care.web.patient.service.PatientAppointmentService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.authentication.AnonymousAuthenticationToken;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;
    import java.util.Map;
    import com.e_health_care.web.doctor.dto.DoctorDTO;
    import com.e_health_care.web.doctor.service.DoctorService;
    import jakarta.servlet.http.Cookie;
    import jakarta.servlet.http.HttpServletResponse;

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
        public ResponseEntity<?> login(@RequestBody DoctorDTO doctorDTO, HttpServletResponse response) {
            String token = authService.verify(doctorDTO);
            if (token != null) {
                response.setHeader("Set-Cookie",
                        "jwt-doctor-token=" + token +
                                "; Path=/; HttpOnly; Max-Age=86400; SameSite=Lax");
    //            return ResponseEntity.ok(Map.of("message", "Login successful"));
                return ResponseEntity.ok(Map.of("message", "Login successful", "token", token));

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
        public ResponseEntity<?> logout(HttpServletResponse response) {
            SecurityContextHolder.clearContext();

            Cookie cookie = new Cookie("jwt-doctor-token", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        }
        @Autowired
        private DoctorService doctorService;


        @GetMapping("/profile")
        public ResponseEntity<?> getProfile() {
            System.out.println(">>> ENTER DOCTOR PROFILE <<<");

            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("AUTH = " + authentication);

                if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                    return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
                }

                String email = authentication.getName();
                System.out.println("EMAIL = " + email);

                Doctor doctor = doctorRepository.findByEmail(email);
                if (doctor == null) {
                    return ResponseEntity.status(404).body(Map.of("error", "Doctor not found"));
                }

                DoctorDTO dto = new DoctorDTO();
                dto.setId(doctor.getId());
                dto.setEmail(doctor.getEmail());
                dto.setFirstName(doctor.getFirstName());
                dto.setLastName(doctor.getLastName());
                dto.setField(doctor.getField());
                dto.setPhone(doctor.getPhone());
                dto.setAddress(doctor.getAddress());
                dto.setAvatar(doctor.getAvatar());

                return ResponseEntity.ok(dto);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
            }
        }

        @PutMapping("/profile/update")
        public ResponseEntity<?> updateProfile(@RequestBody DoctorDTO doctorDTO) {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

                // Lấy doctor hiện tại từ token để có id
                Doctor doctor = doctorRepository.findByEmail(auth.getName());
                if (doctor == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

                doctorDTO.setId(doctor.getId()); // set id từ token
                doctorService.updateDoctorProfile(doctorDTO);
                return ResponseEntity.ok(Map.of("message", "Cập nhật hồ sơ thành công"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }
    }