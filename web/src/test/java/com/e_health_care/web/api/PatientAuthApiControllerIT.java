package com.e_health_care.web.api;

import com.e_health_care.web.AbstractIntegrationTest;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// Fix: thêm import Allure annotations để gắn nhãn nghiệp vụ cho report
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

/**
 * Integration Tests for Patient Registration & Login API.
 *
 * <p>Endpoints under test:
 * <ul>
 *   <li>{@code POST /api/patient/register}</li>
 *   <li>{@code POST /api/patient/login}</li>
 * </ul>
 *
 * <p>Runs against a real H2 database (profile "test") with full Spring
 * Security filter chain — no mocking.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Patient Management")
@Feature("Patient Authentication API")
class PatientAuthApiControllerIT extends AbstractIntegrationTest {

    private static final String REGISTER_URL = "/api/patient/register";
    private static final String LOGIN_URL    = "/api/patient/login";

    /** Pre-seeded patient credentials used by login tests. */
    private static final String SEEDED_EMAIL    = "it_patient_auth@test.com";
    private static final String SEEDED_PASSWORD = "Patient@123";

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────────────────────────────────
    // Data seeding
    // ─────────────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        // Seed one patient that login tests rely on.
        // Guard check prevents duplicate inserts when running ordered tests.
        if (patientRepository.findByEmail(SEEDED_EMAIL).isEmpty()) {
            Patient patient = new Patient();
            patient.setEmail(SEEDED_EMAIL);
            patient.setPassword(passwordEncoder.encode(SEEDED_PASSWORD));
            patient.setFirstName("Auth");
            patient.setLastName("Tester");
            patient.setPhone("0901234567");
            patient.setAddress("100 Integration Avenue");
            patientRepository.save(patient);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds a JSON {@link HttpEntity} from a plain map.
     */
    private <T> HttpEntity<T> jsonEntity(T body) {
        return new HttpEntity<>(body, jsonHeaders());
    }

    // =====================================================================
    //  REGISTER  —  POST /api/patient/register
    // =====================================================================

    @Test
    @Order(1)
    @DisplayName("register — HTTP 200 khi dữ liệu hợp lệ")
    @Story("Patient registration")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Đăng ký bệnh nhân thành công khi dữ liệu hợp lệ, trả về 200 và lưu đúng thông tin vào database")
    void register_shouldReturn200_whenValidInput() {
        Map<String, String> body = Map.of(
                "email",     "new_patient@test.com",
                "password",  "NewPass@123",
                "firstName", "New",
                "lastName",  "Patient",
                "phone",     "0909876543",
                "address",   "200 Test Street"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_URL,
                jsonEntity(body),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody().get("message").toString())
                .containsIgnoringCase("success");

        // Verify persisted in database
        assertThat(patientRepository.findByEmail("new_patient@test.com"))
                .isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getFirstName()).isEqualTo("New");
                    assertThat(p.getLastName()).isEqualTo("Patient");
                });
    }

    @Test
    @Order(2)
    @DisplayName("register — lỗi khi email đã tồn tại")
    @Story("Patient registration")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Đăng ký phải thất bại khi email đã tồn tại trong hệ thống, không được tạo thêm bản ghi trùng")
    void register_shouldReturnError_whenEmailAlreadyExists() {
        // Use the seeded email that already exists in DB
        Map<String, String> body = Map.of(
                "email",     SEEDED_EMAIL,
                "password",  "AnyPass@123",
                "firstName", "Duplicate",
                "lastName",  "User",
                "phone",     "0900000000",
                "address",   "Duplicate Street"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_URL,
                jsonEntity(body),
                Map.class
        );

        // The service throws RuntimeException("Email is already used…")
        // without a global @ControllerAdvice, Spring returns 500.
        assertThat(response.getStatusCode().is4xxClientError()
                || response.getStatusCode().is5xxServerError())
                .as("Duplicate email must NOT succeed (expected 4xx or 5xx, got %s)",
                        response.getStatusCode())
                .isTrue();

        // Confirm only one record with that email exists
        long count = patientRepository.findByEmail(SEEDED_EMAIL).stream().count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Order(3)
    @DisplayName("register — lỗi khi thiếu trường bắt buộc (email rỗng)")
    @Story("Patient registration")
    @Severity(SeverityLevel.NORMAL)
    @Description("Đăng ký phải thất bại khi thiếu các trường bắt buộc (email, password), không được trả về 2xx")
    void register_shouldReturnError_whenInvalidInput() {
        // Missing email & password — controller has no @Valid, so the
        // request may still reach the service. In either case the call
        // should NOT succeed with HTTP 200.
        Map<String, String> body = Map.of(
                "firstName", "NoEmail",
                "lastName",  "User"
                // email & password intentionally omitted
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_URL,
                jsonEntity(body),
                Map.class
        );

        // Without @Valid the service will likely NPE or violate a DB
        // constraint → expect a non-2xx status.
        assertThat(response.getStatusCode().is2xxSuccessful())
                .as("Missing mandatory fields must NOT return 2xx (got %s)",
                        response.getStatusCode())
                .isFalse();
    }

    // =====================================================================
    //  LOGIN  —  POST /api/patient/login
    // =====================================================================

    @Test
    @Order(4)
    @DisplayName("login — HTTP 200 + JWT token khi credentials hợp lệ")
    @Story("Patient login")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Đăng nhập thành công với credentials hợp lệ, trả về JWT token đúng định dạng và cookie jwt-patient-token")
    void login_shouldReturn200_whenValidCredentials() {
        Map<String, String> body = Map.of(
                "email",    SEEDED_EMAIL,
                "password", SEEDED_PASSWORD
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + LOGIN_URL,
                jsonEntity(body),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Token assertions
        String token = (String) response.getBody().get("token");
        assertThat(token)
                .as("Login response must contain a non-blank JWT token")
                .isNotBlank();

        // A JWT has 3 base64-encoded segments separated by dots
        assertThat(token.split("\\."))
                .as("JWT token should have 3 parts (header.payload.signature)")
                .hasSize(3);

        // Message assertion
        assertThat(response.getBody().get("message").toString())
                .containsIgnoringCase("login successful");

        // Set-Cookie header should carry the patient token
        String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie)
                .as("Response should set jwt-patient-token cookie")
                .isNotNull()
                .contains("jwt-patient-token");
    }

    @Test
    @Order(5)
    @DisplayName("login — HTTP 401 khi password sai")
    @Story("Patient login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Đăng nhập phải thất bại với HTTP 401 khi nhập sai password")
    void login_shouldReturn401_whenWrongPassword() {
        Map<String, String> body = Map.of(
                "email",    SEEDED_EMAIL,
                "password", "WrongPassword@999"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + LOGIN_URL,
                jsonEntity(body),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error").toString())
                .containsIgnoringCase("invalid credentials");
    }

    @Test
    @Order(6)
    @DisplayName("login — HTTP 401 khi email không tồn tại")
    @Story("Patient login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Đăng nhập phải thất bại với HTTP 401 khi email không tồn tại trong hệ thống")
    void login_shouldReturn401_whenEmailNotFound() {
        Map<String, String> body = Map.of(
                "email",    "nonexistent@test.com",
                "password", "AnyPass@123"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + LOGIN_URL,
                jsonEntity(body),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error").toString())
                .containsIgnoringCase("invalid credentials");
    }

    // =====================================================================
    //  Edge-case: register rồi login ngay
    // =====================================================================

    @Test
    @Order(7)
    @DisplayName("register rồi login — luồng end-to-end thành công")
    @Story("End-to-end registration and login")
    @Severity(SeverityLevel.NORMAL)
    @Description("Luồng end-to-end: đăng ký tài khoản mới rồi đăng nhập ngay bằng tài khoản đó phải thành công")
    void registerThenLogin_shouldSucceed_endToEnd() {
        // ── 1. Register ──────────────────────────────────────────────────
        String e2eEmail    = "e2e_patient@test.com";
        String e2ePassword = "E2ePass@123";

        Map<String, String> registerBody = Map.of(
                "email",     e2eEmail,
                "password",  e2ePassword,
                "firstName", "E2E",
                "lastName",  "Flow"
        );

        ResponseEntity<Map> registerResp = restTemplate.postForEntity(
                baseUrl() + REGISTER_URL,
                jsonEntity(registerBody),
                Map.class
        );
        assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // ── 2. Login with newly created account ──────────────────────────
        Map<String, String> loginBody = Map.of(
                "email",    e2eEmail,
                "password", e2ePassword
        );

        ResponseEntity<Map> loginResp = restTemplate.postForEntity(
                baseUrl() + LOGIN_URL,
                jsonEntity(loginBody),
                Map.class
        );

        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) loginResp.getBody().get("token")).isNotBlank();
    }
}