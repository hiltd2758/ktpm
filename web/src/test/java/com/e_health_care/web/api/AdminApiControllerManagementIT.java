package com.e_health_care.web.api;

import com.e_health_care.web.AbstractIntegrationTest;
import com.e_health_care.web.admin.model.Admin;
import com.e_health_care.web.admin.repository.AdminRepository;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EHC-25 — Integration Tests for AdminApiController (Management)
 * Covers: Dashboard, Statistics, Doctor Management, Patient Management
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminApiControllerManagementIT extends AbstractIntegrationTest {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Shared state across tests (static so it survives between @BeforeEach calls)
    private static String adminToken;
    private static Long seededDoctorId;
    private static Long seededPatientId;

    // ─────────────────────────────────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        // ── Seed Admin ──────────────────────────────────────────────────────
        if (adminRepository.findByEmail("it_admin@test.com") == null) {
            Admin admin = new Admin();
            admin.setEmail("it_admin@test.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            adminRepository.save(admin);
        }

        // ── Seed Doctor ──────────────────────────────────────────────────────
        if (doctorRepository.findByEmail("it_mgmt_doctor@test.com") == null) {
            Doctor doctor = new Doctor();
            doctor.setEmail("it_mgmt_doctor@test.com");
            doctor.setPassword(passwordEncoder.encode("Doctor@123"));
            doctor.setFirstName("Mgmt");
            doctor.setLastName("Doctor");
            doctor.setField("Cardiology");
            doctor.setPhone("0901111111");
            doctor.setAddress("123 Test Street");
            doctor.setROLE("ROLE_DOCTOR");
            Doctor saved = doctorRepository.save(doctor);
            seededDoctorId = saved.getId();
        } else {
            seededDoctorId = doctorRepository.findByEmail("it_mgmt_doctor@test.com").getId();
        }

        // ── Seed Patient ─────────────────────────────────────────────────────
        if (patientRepository.findByEmail("it_mgmt_patient@test.com").isEmpty()) {
            Patient patient = new Patient();
            patient.setEmail("it_mgmt_patient@test.com");
            patient.setPassword(passwordEncoder.encode("Patient@123"));
            patient.setFirstName("Mgmt");
            patient.setLastName("Patient");
            patient.setPhone("0902222222");
            patient.setAddress("456 Test Avenue");
            Patient saved = patientRepository.save(patient);
            seededPatientId = saved.getId();
        } else {
            seededPatientId = patientRepository.findByEmail("it_mgmt_patient@test.com")
                    .get().getId();
        }

        // ── Login Admin (once) ───────────────────────────────────────────────
        if (adminToken == null) {
            HttpHeaders headers = jsonHeaders();
            Map<String, String> body = Map.of(
                    "email", "it_admin@test.com",
                    "password", "Admin@123"
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl() + "/api/admin/login",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            assertEquals(HttpStatus.OK, response.getStatusCode(),
                    "Admin login during setup should succeed");
            adminToken = (String) response.getBody().get("token");
            assertNotNull(adminToken, "Admin token must not be null after login");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bước 3 – Dashboard & Statistics
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("IT_ADMIN_01 — getDashboard: Admin hợp lệ → HTTP 200")
    void getDashboard_shouldReturn200_whenAdminAuthenticated() {
        HttpHeaders headers = adminCookieHeader(adminToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("doctors"),
                "Response phải chứa key 'doctors'");
        assertTrue(response.getBody().containsKey("patients"),
                "Response phải chứa key 'patients'");
    }

    @Test
    @Order(2)
    @DisplayName("IT_ADMIN_02 — getDashboard: không có token → HTTP 401 hoặc 403")
    void getDashboard_shouldReturn401_whenNoToken() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/api/admin/dashboard",
                Map.class
        );

        assertTrue(
                response.getStatusCode() == HttpStatus.UNAUTHORIZED
                        || response.getStatusCode() == HttpStatus.FORBIDDEN,
                "Phải trả về 401 hoặc 403 khi không có token, nhưng nhận được: "
                        + response.getStatusCode()
        );
    }

    @Test
    @Order(3)
    @DisplayName("IT_ADMIN_03 — getStatistics: Admin hợp lệ → HTTP 200 + các key thống kê")
    void getStatistics_shouldReturn200_whenAdminAuthenticated() {
        HttpHeaders headers = adminCookieHeader(adminToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/statistics",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("totalDoctors"),   "Thiếu key 'totalDoctors'");
        assertTrue(response.getBody().containsKey("totalPatients"),  "Thiếu key 'totalPatients'");
        assertTrue(response.getBody().containsKey("totalAppointments"), "Thiếu key 'totalAppointments'");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bước 4 – Doctor Management
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("IT_ADMIN_04 — updateDoctorInfo: dữ liệu hợp lệ → HTTP 200")
    void updateDoctorInfo_shouldReturn200_whenValid() {
        // Build updated doctor payload (id bắt buộc để controller tìm existing)
        Map<String, Object> doctorPayload = Map.of(
                "id",        seededDoctorId,
                "email",     "it_mgmt_doctor@test.com",
                "firstName", "MgmtUpdated",
                "lastName",  "DoctorUpdated",
                "field",     "Neurology",
                "phone",     "0901111199",
                "address",   "789 Updated Street",
                "ROLE",      "ROLE_DOCTOR"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", "jwt-admin-token=" + adminToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/doctor/update/info",
                HttpMethod.PUT,
                new HttpEntity<>(doctorPayload, headers),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("message"),
                "Response phải chứa key 'message'");
    }

    @Test
    @Order(5)
    @DisplayName("IT_ADMIN_05 — updateDoctorPassword: dữ liệu hợp lệ → HTTP 200")
    void updateDoctorPassword_shouldReturn200_whenValid() {
        Map<String, String> body = Map.of("newPassword", "NewDoctor@456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", "jwt-admin-token=" + adminToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/doctor/update/password/" + seededDoctorId,
                HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("message"),
                "Response phải chứa key 'message'");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bước 5 – Patient Management
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("IT_ADMIN_06 — updatePatientInfo: dữ liệu hợp lệ → HTTP 200")
    void updatePatientInfo_shouldReturn200_whenValid() {
        Map<String, Object> patientPayload = Map.of(
                "id",        seededPatientId,
                "email",     "it_mgmt_patient@test.com",
                "firstName", "MgmtUpdated",
                "lastName",  "PatientUpdated",
                "phone",     "0902222299",
                "address",   "789 Updated Avenue"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", "jwt-admin-token=" + adminToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/patient/update/info",
                HttpMethod.PUT,
                new HttpEntity<>(patientPayload, headers),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("message"),
                "Response phải chứa key 'message'");
    }

    @Test
    @Order(7)
    @DisplayName("IT_ADMIN_07 — deletePatient: patient tồn tại → HTTP 200")
    void deletePatient_shouldReturn200_whenFound() {
        // Tạo thêm một patient mới để delete, tránh ảnh hưởng test khác
        Patient toDelete = new Patient();
        toDelete.setEmail("it_delete_patient@test.com");
        toDelete.setPassword(passwordEncoder.encode("Delete@123"));
        toDelete.setFirstName("Delete");
        toDelete.setLastName("Me");
        toDelete.setPhone("0903333333");
        toDelete.setAddress("To Be Deleted");
        Long deleteId = patientRepository.save(toDelete).getId();

        HttpHeaders headers = adminCookieHeader(adminToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/patient/delete/" + deleteId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("message"),
                "Response phải chứa key 'message'");

        // Xác nhận đã xóa khỏi DB
        assertTrue(patientRepository.findById(deleteId).isEmpty(),
                "Patient phải đã bị xóa khỏi database");
    }
}
