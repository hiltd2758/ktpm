package com.e_health_care.web.patient.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * EHC-62 — Regression test cho Lỗi 3:
 * "Lưu mật khẩu dạng Text trần (Plain Text) khi cập nhật Profile".
 *
 * Nguyên nhân gốc: PatientUpdateProfileService dòng 67-69 gán trực tiếp
 * patientDTO.getPassword() vào entity, không mã hóa qua PasswordEncoder.
 * Hậu quả: lộ mật khẩu trong DB + user không đăng nhập lại được vì
 * passwordEncoder.matches() so khớp hash, không so khớp plain text.
 */
@Epic("Patient Management")
@Feature("Patient Profile Update")
class PatientUpdateProfilePasswordEncodingTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientUpdateProfileService service;

    @Test
    @Story("Bệnh nhân đổi mật khẩu khi cập nhật hồ sơ")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Khi bệnh nhân nhập mật khẩu mới trong form cập nhật hồ sơ, " +
            "hệ thống phải mã hóa (hash) mật khẩu trước khi lưu vào DB, " +
            "TUYỆT ĐỐI không được lưu dạng văn bản trần.")
    void updatePatient_withNewPassword_shouldEncodeBeforeSaving() {
        Patient existing = new Patient();
        existing.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newRawPassword123")).thenReturn("$2a$10$hashed.value.example");

        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPassword("newRawPassword123");

        service.updatePatient(dto, 1L);

        // Password lưu vào entity phải là bản đã hash, không phải raw password
        assertNotEquals("newRawPassword123", existing.getPassword());
        assertEquals("$2a$10$hashed.value.example", existing.getPassword());
        verify(passwordEncoder, times(1)).encode("newRawPassword123");
    }

    @Test
    @Story("Bệnh nhân cập nhật hồ sơ nhưng KHÔNG đổi mật khẩu")
    @Severity(SeverityLevel.NORMAL)
    @Description("Khi trường password trong DTO rỗng/null (người dùng không đổi " +
            "mật khẩu), hệ thống không được gọi encode() và không được ghi đè " +
            "mật khẩu cũ đang lưu.")
    void updatePatient_withoutPassword_shouldNotTouchPassword() {
        Patient existing = new Patient();
        existing.setId(1L);
        existing.setPassword("$2a$10$old.hashed.password");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));

        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setPassword(""); // không đổi mật khẩu

        service.updatePatient(dto, 1L);

        assertEquals("$2a$10$old.hashed.password", existing.getPassword());
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.anyString());
    }
}