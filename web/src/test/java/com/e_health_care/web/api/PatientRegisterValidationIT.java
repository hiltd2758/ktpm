package com.e_health_care.web.api;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.e_health_care.web.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EHC-62 — Regression test cho Lỗi 1:
 * "Hệ thống không kiểm tra tính hợp lệ dữ liệu đầu vào (thiếu @Valid)".
 *
 * Trước khi fix: PatientAuthApiController.register() không có @Valid,
 * nên các ràng buộc @NotBlank/@Email/@Size trong PatientDTO bị bỏ qua
 * hoàn toàn, request sai vẫn được lưu vào DB với HTTP 200.
 *
 * Sau khi fix: request sai định dạng phải bị chặn ở tầng validation
 * và trả về HTTP 400, KHÔNG được chạm tới service/DB.
 */
class PatientRegisterValidationIT extends AbstractIntegrationTest {

    private static final String REGISTER_URL = "/api/patient/register";

    private HttpEntity<Map<String, String>> jsonEntity(Map<String, String> body) {
        return new HttpEntity<>(body, jsonHeaders());
    }

    @Test
    @DisplayName("register — HTTP 400 khi email sai định dạng (thiếu ký tự @)")
    void register_shouldReturn400_whenEmailFormatInvalid() {
        Map<String, String> body = Map.of(
                "email", "invalid-email-format", // thiếu @
                "password", "ValidPass123",
                "firstName", "Invalid",
                "lastName", "Email"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_URL, jsonEntity(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("register — HTTP 400 khi mật khẩu ngắn hơn 8 ký tự")
    void register_shouldReturn400_whenPasswordTooShort() {
        Map<String, String> body = Map.of(
                "email", "shortpass@example.com",
                "password", "123", // < 8 ký tự, vi phạm @Size(min = 8)
                "firstName", "Short",
                "lastName", "Pass"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_URL, jsonEntity(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("register — HTTP 400 khi thiếu firstName/lastName bắt buộc")
    void register_shouldReturn400_whenRequiredFieldsMissing() {
        Map<String, String> body = Map.of(
                "email", "missingfields@example.com",
                "password", "ValidPass123"
                // firstName, lastName bị thiếu -> vi phạm @NotBlank
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_URL, jsonEntity(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("register — HTTP 200 khi dữ liệu hợp lệ đầy đủ (sanity check)")
    void register_shouldReturn200_whenAllFieldsValid() {
        Map<String, String> body = Map.of(
                "email", "valid.ehc62@example.com",
                "password", "ValidPass123",
                "firstName", "Valid",
                "lastName", "User",
                "phone", "0901112222",
                "address", "123 Valid Street"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_URL, jsonEntity(body), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}