package com.e_health_care.web.patient.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
 * EHC-62 — Regression test cho Lỗi 2:
 * "Giá trị số điện thoại trống (null) bị biến đổi thành chữ 'null'".
 *
 * Nguyên nhân gốc: PatientAuthenticationService dòng 39 dùng
 * String.valueOf(patientDTO.getPhone()) — String.valueOf(null) trả về
 * chuỗi văn bản "null" (4 ký tự) thay vì giá trị null thực sự.
 */
@Epic("Patient Management")
@Feature("Patient Registration")
class PatientRegisterPhoneNullFixTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientAuthenticationService service;

    @Test
    @Story("Đăng ký bệnh nhân để trống số điện thoại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Khi bệnh nhân đăng ký không nhập số điện thoại (phone = null), " +
            "hệ thống phải lưu giá trị null thực sự vào entity, KHÔNG được lưu " +
            "thành chuỗi văn bản \"null\" (4 ký tự).")
    void register_withNullPhone_shouldStoreActualNull_notStringNull() {
        PatientDTO dto = new PatientDTO();
        dto.setEmail("no.phone@example.com");
        dto.setPassword("secret123");
        dto.setPhone(null); // Bệnh nhân bỏ trống số điện thoại

        when(patientRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed-password");
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient saved = service.register(dto);

        // Trước khi fix: saved.getPhone() == "null" (String.valueOf(null))
        // Sau khi fix:   saved.getPhone() == null
        assertNull(saved.getPhone(), "Phone phải là null thực sự, không phải chuỗi \"null\"");
        assertNotEquals("null", saved.getPhone());
    }

    @Test
    @Story("Đăng ký bệnh nhân có nhập số điện thoại hợp lệ")
    @Severity(SeverityLevel.NORMAL)
    @Description("Khi bệnh nhân nhập số điện thoại hợp lệ, giá trị phải được lưu " +
            "chính xác, không bị biến đổi.")
    void register_withValidPhone_shouldStoreExactValue() {
        PatientDTO dto = new PatientDTO();
        dto.setEmail("has.phone@example.com");
        dto.setPassword("secret123");
        dto.setPhone("0901234567");

        when(patientRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed-password");
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient saved = service.register(dto);

        assertEquals("0901234567", saved.getPhone());
    }
}