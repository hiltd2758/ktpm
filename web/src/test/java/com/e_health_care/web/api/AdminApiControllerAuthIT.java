package com.e_health_care.web.api;

import com.e_health_care.web.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

@Epic("Admin Management")
@Feature("Admin Authentication API")
class AdminApiControllerAuthIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String adminEmail = "admin_it_test@example.com";
    private final String adminPassword = "AdminPassword123!";

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM admin WHERE email = ?", adminEmail);

        String encodedPassword = passwordEncoder.encode(adminPassword);
        jdbcTemplate.update("INSERT INTO admin (email, password, role) VALUES (?, ?, 'ADMIN')", adminEmail, encodedPassword);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM admin WHERE email = ?", adminEmail);
    }

    // ==========================================
    // BƯỚC 3: TEST LUỒNG LOGIN
    // ==========================================

    @Test
    @Story("Đăng nhập với thông tin hợp lệ")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra API đăng nhập trả về HTTP 200 và JWT token khi admin đăng nhập với email/mật khẩu đúng.")
    void login_shouldReturn200_whenValidCredentials() {
        Map<String, String> request = new HashMap<>();
        request.put("email", adminEmail);
        request.put("password", adminPassword);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/admin/login", request, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        boolean hasToken = response.getBody().containsKey("token") || response.getBody().containsKey("accessToken");
        assertTrue(hasToken, "API phải trả về JWT Token khi đăng nhập thành công");
    }

    @Test
    @Story("Đăng nhập với mật khẩu sai")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra API đăng nhập trả về HTTP 401 khi mật khẩu nhập vào không đúng.")
    void login_shouldReturn401_whenWrongPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("email", adminEmail);
        request.put("password", "SaiMatKhau123!"); // Pass sai

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/admin/login", request, Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Story("Đăng nhập với email không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra API đăng nhập trả về HTTP 401 khi email không tồn tại trong hệ thống.")
    void login_shouldReturn401_whenEmailNotFound() {
        Map<String, String> request = new HashMap<>();
        request.put("email", "notfound@example.com"); // Email ảo
        request.put("password", adminPassword);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/admin/login", request, Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Story("Đăng nhập thiếu trường bắt buộc")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra API đăng nhập trả về HTTP 401 khi thiếu trường password trong request.")
    void login_shouldReturn401_whenMissingFields() {
        Map<String, String> request = new HashMap<>();
        request.put("email", adminEmail); // Thiếu field password

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/admin/login", request, Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ==========================================
    // BƯỚC 4: TEST LUỒNG LOGOUT
    // ==========================================

    @Test
    @Story("Đăng xuất khi đã xác thực")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra API đăng xuất trả về HTTP 200 khi request có kèm JWT token hợp lệ.")
    void logout_shouldReturn200_whenAuthenticated() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", adminEmail);
        loginRequest.put("password", adminPassword);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity("/api/admin/login", loginRequest, Map.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());

        String token = (String) loginResponse.getBody().getOrDefault("token", loginResponse.getBody().get("accessToken"));
        assertNotNull(token, "Không lấy được token từ API Login");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> logoutResponse = restTemplate.postForEntity("/api/admin/logout", requestEntity, Map.class);

        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
    }

    @Test
    @Story("Đăng xuất khi không có token")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra API đăng xuất trả về HTTP 401 hoặc 403 khi request không kèm token xác thực.")
    void logout_shouldReturn401_whenNoToken() {
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/admin/logout", null, Map.class);

        assertTrue(
                response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.FORBIDDEN,
                "Phải văng lỗi 401 hoặc 403 khi không có Token"
        );
    }
}