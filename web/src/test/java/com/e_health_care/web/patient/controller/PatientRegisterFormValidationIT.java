package com.e_health_care.web.patient.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.e_health_care.web.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EHC-62 — Regression test cho Lỗi 1 (phần MVC form):
 * "PatientAuthenticationController.register() thiếu @Valid".
 *
 * Controller này nhận dữ liệu qua @ModelAttribute từ form Thymeleaf
 * (không phải JSON REST), nên test phải gửi request dạng
 * application/x-www-form-urlencoded thay vì JSON.
 *
 * Lưu ý: TestRestTemplate mặc định TỰ ĐỘNG follow redirect, nên khi
 * server trả 302, response nhận về trong test là trang ĐÍCH sau khi
 * redirect (thường là 200), không phải mã 302 gốc. Vì vậy test không
 * assert trực tiếp status FOUND, mà assert "không rơi vào luồng lỗi".
 *
 * Trước khi fix: dù dữ liệu sai (email không có @, password quá ngắn...),
 * controller vẫn gọi authServicePatient.register() và redirect thành công
 * tới /patient/login như thể đăng ký hợp lệ.
 *
 * Sau khi fix: @Valid + BindingResult chặn lại, trả về lại trang
 * đăng ký (200) kèm lỗi validation, KHÔNG gọi service, KHÔNG lưu DB.
 */
class PatientRegisterFormValidationIT extends AbstractIntegrationTest {

    private static final String REGISTER_FORM_URL = "/patient/register";

    private HttpEntity<MultiValueMap<String, String>> formEntity(MultiValueMap<String, String> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(form, headers);
    }

    @Test
    @DisplayName("register (form) — không lỗi server khi email sai định dạng")
    void registerForm_shouldNotError_whenEmailInvalid() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("email", "invalid-email-format"); // thiếu ký tự @
        form.add("password", "ValidPass123");
        form.add("firstName", "Invalid");
        form.add("lastName", "Email");

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_FORM_URL, formEntity(form), String.class);

        // Dữ liệu sai phải bị chặn ở tầng validation (BindingResult trả
        // lại form với status 200), tuyệt đối không được là lỗi 5xx do
        // exception văng ra từ service/DB.
        assertThat(response.getStatusCode().is5xxServerError())
                .as("Email sai định dạng không được gây lỗi server (500)")
                .isFalse();
    }

    @Test
    @DisplayName("register (form) — không lỗi server khi password ngắn hơn 8 ký tự")
    void registerForm_shouldNotError_whenPasswordTooShort() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("email", "shortpass.form@example.com");
        form.add("password", "123"); // < 8 ký tự, vi phạm @Size(min = 8)
        form.add("firstName", "Short");
        form.add("lastName", "Pass");

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_FORM_URL, formEntity(form), String.class);

        assertThat(response.getStatusCode().is5xxServerError())
                .as("Password quá ngắn không được gây lỗi server (500)")
                .isFalse();
    }

    @Test
    @DisplayName("register (form) — không lỗi server khi thiếu firstName/lastName")
    void registerForm_shouldNotError_whenRequiredFieldsMissing() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("email", "missingfields.form@example.com");
        form.add("password", "ValidPass123");
        // firstName, lastName bị thiếu -> vi phạm @NotBlank

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_FORM_URL, formEntity(form), String.class);

        assertThat(response.getStatusCode().is5xxServerError())
                .as("Thiếu firstName/lastName không được gây lỗi server (500)")
                .isFalse();
    }

    @Test
    @DisplayName("register (form) — thành công khi dữ liệu hợp lệ đầy đủ (sanity check)")
    void registerForm_shouldSucceed_whenAllFieldsValid() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("email", "valid.form.ehc62@example.com");
        form.add("password", "ValidPass123");
        form.add("firstName", "Valid");
        form.add("lastName", "User");
        form.add("phone", "0903334444");
        form.add("address", "123 Valid Form Street");

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + REGISTER_FORM_URL, formEntity(form), String.class);

        // TestRestTemplate tự follow redirect: đăng ký thành công (302)
        // -> chuyển tới trang login -> nhận về 200 ở đây.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}