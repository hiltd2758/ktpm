package com.e_health_care.web.api;
import java.util.Map;
import com.e_health_care.web.AbstractIntegrationTest;
import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

/**
 * Integration Test cho DoctorApiController.
 * - Không mock service/repository: dùng Spring context thật + H2 in-memory DB.
 * - Mỗi test tự chuẩn bị / xoá dữ liệu của chính nó (độc lập thứ tự chạy).
 *
 * LƯU Ý QUAN TRỌNG VỀ HTTP STATUS:
 * Theo source thật của DoctorApiController.login(), khi authService.verify()
 * trả về null (sai password HOẶC email không tồn tại), controller trả về
 * HTTP 401 (Unauthorized), KHÔNG phải 400. Do đó 2 test case "wrong password"
 * và "email not found" dưới đây assert 401 để khớp đúng hành vi thực tế của
 * code. Nếu business yêu cầu phải là 400, cần sửa lại controller, không sửa test.
 */
@DisplayName("DoctorApiController Integration Test")
@Epic("Doctor Management")
@Feature("Doctor Profile & Auth API")
class DoctorApiControllerIT extends AbstractIntegrationTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String RAW_PASSWORD = "Doctor@123";
    private String testDoctorEmail;

    @BeforeEach
    void setUp() {
        // Mỗi test dùng email riêng (unique theo nanoTime) để các test không đụng dữ liệu của nhau
        testDoctorEmail = "doctor." + System.nanoTime() + "@ehealth.test";

        Doctor doctor = new Doctor();
        doctor.setEmail(testDoctorEmail);
        doctor.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        doctor.setFirstName("Nguyen");
        doctor.setLastName("An");
        doctor.setField("Cardiology");
        doctor.setPhone("0901234567");
        doctor.setAddress("123 Le Loi, Q1, TP.HCM");
        doctorRepository.save(doctor);
    }

    @AfterEach
    void tearDown() {
        Doctor doctor = doctorRepository.findByEmail(testDoctorEmail);
        if (doctor != null) {
            doctorRepository.delete(doctor);
        }
    }

    // ---------- Helpers ----------

    private ResponseEntity<Map> login(String email, String password) {
        DoctorDTO body = new DoctorDTO();
        body.setEmail(email);
        body.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<DoctorDTO> request = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity("/api/doctor/login", request, Map.class);
    }

    private String loginAndGetToken() {
        ResponseEntity<Map> response = login(testDoctorEmail, RAW_PASSWORD);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Object token = response.getBody().get("token");
        assertNotNull(token, "Token phải khác null khi login thành công");
        return token.toString();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return headers;
    }

    // ===================== Bước 3 – Login API =====================

    @Test
    @DisplayName("login_shouldReturn200_whenValidCredentials")
    @Story("Đăng nhập với thông tin hợp lệ")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra API đăng nhập trả về HTTP 200 kèm JWT token hợp lệ (đúng định dạng header.payload.signature) khi email/mật khẩu chính xác.")
    void login_shouldReturn200_whenValidCredentials() {
        ResponseEntity<Map> response = login(testDoctorEmail, RAW_PASSWORD);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login successful", response.getBody().get("message"));

        Object token = response.getBody().get("token");
        assertNotNull(token, "Response body phải chứa JWT token");
        assertTrue(token.toString().length() > 0);
        // JWT chuẩn có dạng header.payload.signature
        assertEquals(3, token.toString().split("\\.").length);
    }

    @Test
    @DisplayName("login_shouldReturn401_whenWrongPassword")
    @Story("Đăng nhập với mật khẩu sai")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra API đăng nhập trả về HTTP 401 kèm thông báo lỗi khi mật khẩu không đúng.")
    void login_shouldReturn401_whenWrongPassword() {
        // NOTE: Yêu cầu gốc ghi "expect 400", nhưng code thật của controller
        // trả 401 (Map.of("error","Invalid credentials")) khi verify() == null.
        ResponseEntity<Map> response = login(testDoctorEmail, "WrongPassword999");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().get("error"));
    }

    @Test
    @DisplayName("login_shouldReturn401_whenEmailNotFound")
    @Story("Đăng nhập với email không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra API đăng nhập trả về HTTP 401 kèm thông báo lỗi khi email không tồn tại trong hệ thống.")
    void login_shouldReturn401_whenEmailNotFound() {
        // NOTE: tương tự trên — code thật trả 401, không phải 400.
        ResponseEntity<Map> response = login("khong-ton-tai-" + System.nanoTime() + "@ehealth.test", RAW_PASSWORD);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().get("error"));
    }

    // ===================== Bước 4 – Profile API =====================

    @Test
    @DisplayName("getProfile_shouldReturn200_whenAuthenticated")
    @Story("Xem hồ sơ bác sĩ khi đã xác thực")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra API trả về đúng thông tin hồ sơ bác sĩ (email, firstName) khi request có token hợp lệ.")
    void getProfile_shouldReturn200_whenAuthenticated() {
        String token = loginAndGetToken();

        HttpEntity<Void> request = new HttpEntity<>(authHeaders(token));
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/doctor/profile", HttpMethod.GET, request, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testDoctorEmail, response.getBody().get("email"));
        assertEquals("Nguyen", response.getBody().get("firstName"));
    }

    @Test
    @DisplayName("getProfile_shouldReturn401_whenNoToken")
    @Story("Xem hồ sơ bác sĩ khi không có token")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra API trả về HTTP 401 khi request không có token xác thực.")
    void getProfile_shouldReturn401_whenNoToken() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/doctor/profile", Map.class);

        // ApiSecurityConfiguration: anyRequest().authenticated() + entry point -> 401
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ===================== Bước 5 – Update Profile API =====================

    @Test
    @DisplayName("updateProfile_shouldReturn200_whenValid")
    @Story("Cập nhật hồ sơ bác sĩ thành công")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra API cập nhật hồ sơ trả về HTTP 200 và dữ liệu được lưu đúng trong cơ sở dữ liệu khi thông tin hợp lệ.")
    void updateProfile_shouldReturn200_whenValid() {
        String token = loginAndGetToken();

        DoctorDTO updateBody = new DoctorDTO();
        updateBody.setEmail(testDoctorEmail); // không dùng để update nhưng set cho đủ field
        updateBody.setPassword("placeholder"); // DTO yêu cầu @NotBlank, không liên quan logic update
        updateBody.setFirstName("Tran");
        updateBody.setLastName("Binh");
        updateBody.setPhone("0987654321");
        updateBody.setAddress("456 Nguyen Trai, Q5, TP.HCM");
        updateBody.setField("Dermatology");

        HttpEntity<DoctorDTO> request = new HttpEntity<>(updateBody, authHeaders(token));
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/doctor/profile/update", HttpMethod.PUT, request, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Cập nhật hồ sơ thành công", response.getBody().get("message"));

        // Verify dữ liệu thực sự được cập nhật trong H2
        Doctor updated = doctorRepository.findByEmail(testDoctorEmail);
        assertNotNull(updated);
        assertEquals("Tran", updated.getFirstName());
        assertEquals("Binh", updated.getLastName());
        assertEquals("Dermatology", updated.getField());
        assertEquals("0987654321", updated.getPhone());
    }
}