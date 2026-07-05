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

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

/**
 * EHC-25 — Integration Tests for AdminApiController (Management)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Admin Management")
@Feature("Admin Dashboard & CRUD API")
class AdminApiControllerManagementIT extends AbstractIntegrationTest {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String adminToken;
    private static Long seededDoctorId;
    private static Long seededPatientId;

    // ── Helper: gửi token qua Authorization header (Bearer) ──────────────────
    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    private HttpHeaders bearerJsonHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        // ── Seed Admin ───────────────────────────────────────────────────────
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
            seededDoctorId = doctorRepository.save(doctor).getId();
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
            seededPatientId = patientRepository.save(patient).getId();
        } else {
            seededPatientId = patientRepository.findByEmail("it_mgmt_patient@test.com")
                    .get().getId();
        }

        // ── Login Admin lấy token (chỉ 1 lần) ───────────────────────────────
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
                    "Admin login trong setup phải thành công");
            adminToken = (String) response.getBody().get("token");
            assertNotNull(adminToken, "Token không được null sau khi login");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bước 3 – Dashboard & Statistics
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("IT_ADMIN_01 — getDashboard: Admin hợp lệ → HTTP 200")
    @Story("Xem dashboard khi admin đã xác thực")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra API dashboard trả về HTTP 200 kèm thông tin doctors, patients khi admin đã đăng nhập hợp lệ.")
    void getDashboard_shouldReturn200_whenAdminAuthenticated() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/dashboard",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(adminToken)),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("doctors"), "Thiếu key 'doctors'");
        assertTrue(response.getBody().containsKey("patients"), "Thiếu key 'patients'");
    }

    @Test
    @Order(2)
    @DisplayName("IT_ADMIN_02 — getDashboard: không có token → HTTP 401 hoặc 403")
    @Story("Xem dashboard khi không có token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra API dashboard trả về HTTP 401 hoặc 403 khi request không có token xác thực.")
    void getDashboard_shouldReturn401_whenNoToken() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/api/admin/dashboard",
                Map.class
        );

        assertTrue(
                response.getStatusCode() == HttpStatus.UNAUTHORIZED
                        || response.getStatusCode() == HttpStatus.FORBIDDEN,
                "Phải trả về 401 hoặc 403, nhưng nhận: " + response.getStatusCode()
        );
    }

    @Test
    @Order(3)
    @DisplayName("IT_ADMIN_03 — getStatistics: Admin hợp lệ → HTTP 200 + các key thống kê")
    @Story("Xem thống kê hệ thống")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra API thống kê trả về HTTP 200 kèm đầy đủ các key totalDoctors, totalPatients, totalAppointments.")
    void getStatistics_shouldReturn200_whenAdminAuthenticated() {
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/statistics",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(adminToken)),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("totalDoctors"),      "Thiếu key 'totalDoctors'");
        assertTrue(response.getBody().containsKey("totalPatients"),     "Thiếu key 'totalPatients'");
        assertTrue(response.getBody().containsKey("totalAppointments"), "Thiếu key 'totalAppointments'");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bước 4 – Doctor Management
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("IT_ADMIN_04 — updateDoctorInfo: dữ liệu hợp lệ → HTTP 200")
    @Story("Cập nhật thông tin bác sĩ")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra API cập nhật thông tin bác sĩ trả về HTTP 200 khi dữ liệu đầu vào hợp lệ.")
    void updateDoctorInfo_shouldReturn200_whenValid() {
        Map<String, Object> payload = Map.of(
                "id",        seededDoctorId,
                "email",     "it_mgmt_doctor@test.com",
                "firstName", "MgmtUpdated",
                "lastName",  "DoctorUpdated",
                "field",     "Neurology",
                "phone",     "0901111199",
                "address",   "789 Updated Street",
                "ROLE",      "ROLE_DOCTOR"
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/doctor/update/info",
                HttpMethod.PUT,
                new HttpEntity<>(payload, bearerJsonHeaders(adminToken)),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("message"), "Thiếu key 'message'");
    }

    @Test
    @Order(5)
    @DisplayName("IT_ADMIN_05 — updateDoctorPassword: dữ liệu hợp lệ → HTTP 200")
    @Story("Cập nhật mật khẩu bác sĩ")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra API cập nhật mật khẩu bác sĩ trả về HTTP 200 khi mật khẩu mới hợp lệ.")
    void updateDoctorPassword_shouldReturn200_whenValid() {
        Map<String, String> body = Map.of("newPassword", "NewDoctor@456");

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/doctor/update/password/" + seededDoctorId,
                HttpMethod.PUT,
                new HttpEntity<>(body, bearerJsonHeaders(adminToken)),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("message"), "Thiếu key 'message'");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bước 5 – Patient Management
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("IT_ADMIN_06 — updatePatientInfo: dữ liệu hợp lệ → HTTP 200")
    @Story("Cập nhật thông tin bệnh nhân")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra API cập nhật thông tin bệnh nhân trả về HTTP 200 khi dữ liệu đầu vào hợp lệ.")
    void updatePatientInfo_shouldReturn200_whenValid() {
        Map<String, Object> payload = Map.of(
                "id",        seededPatientId,
                "email",     "it_mgmt_patient@test.com",
                "firstName", "MgmtUpdated",
                "lastName",  "PatientUpdated",
                "phone",     "0902222299",
                "address",   "789 Updated Avenue"
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/patient/update/info",
                HttpMethod.PUT,
                new HttpEntity<>(payload, bearerJsonHeaders(adminToken)),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("message"), "Thiếu key 'message'");
    }

    @Test
    @Order(7)
    @DisplayName("IT_ADMIN_07 — deletePatient: patient tồn tại → HTTP 200")
    @Story("Xóa bệnh nhân đã tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra API xóa bệnh nhân trả về HTTP 200 và bản ghi bị xóa khỏi cơ sở dữ liệu khi patientId tồn tại.")
    void deletePatient_shouldReturn200_whenFound() {
        // Tạo patient riêng để delete, không ảnh hưởng test khác
        Patient toDelete = new Patient();
        toDelete.setEmail("it_delete_patient@test.com");
        toDelete.setPassword(passwordEncoder.encode("Delete@123"));
        toDelete.setFirstName("Delete");
        toDelete.setLastName("Me");
        toDelete.setPhone("0903333333");
        toDelete.setAddress("To Be Deleted");
        Long deleteId = patientRepository.save(toDelete).getId();

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/admin/patient/delete/" + deleteId,
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(adminToken)),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("message"), "Thiếu key 'message'");

        // Xác nhận đã xóa khỏi DB
        assertTrue(patientRepository.findById(deleteId).isEmpty(),
                "Patient phải đã bị xóa khỏi database");
    }
}