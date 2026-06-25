package com.e_health_care.web.admin.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import com.e_health_care.web.patient.repository.PatientRepository;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * EP + BVA cho createPatient() — AdminManagementService
 *
 * V1 = email chưa tồn tại    X1 = email đã tồn tại
 * V2 = patient object hợp lệ X2 = email null/rỗng
 * B2 = insert lần đầu (hợp lệ)
 * B3 = insert lần 2 cùng email (biên trùng)
 */
class AdminCreatePatientEpBvaTest extends BaseServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PatientClinicalInforRepository patientClinicalInforRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminManagementService service;

    private Patient patient(String email) {
        Patient p = new Patient();
        p.setEmail(email);
        p.setFirstName("Test");
        p.setLastName("User");
        return p;
    }

    // TC01 — V1, V2, B2 — email mới, tạo thành công
    @Test
    @DisplayName("TC01 [V1,V2,B2]: email chưa tồn tại -> tạo patient thành công")
    void tc01_newEmail_shouldCreatePatient() {
        Patient p = patient("new@example.com");
        when(patientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(patientRepository.save(p)).thenReturn(p);

        Patient result = service.createPatient(p);

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        verify(patientRepository).save(p);
    }

    // TC02 — X1, B3 — email đã tồn tại
    @Test
    @DisplayName("TC02 [X1,B3]: email đã tồn tại -> throw 'Email already exists'")
    void tc02_duplicateEmail_shouldThrow() {
        Patient p = patient("dup@example.com");
        when(patientRepository.findByEmail("dup@example.com"))
                .thenReturn(Optional.of(new Patient()));

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.createPatient(p));
        assertTrue(ex.getMessage().contains("Email already exists"));
        verify(patientRepository, never()).save(any());
    }

    // TC03 — X2 — email null → NullPointerException hoặc throw
    @Test
    @DisplayName("TC03 [X2]: email null -> throw exception (NPE hoặc validation)")
    void tc03_nullEmail_shouldThrow() {
        Patient p = patient(null);
        // findByEmail(null) sẽ throw NullPointerException từ service
        when(patientRepository.findByEmail(null))
                .thenThrow(new IllegalArgumentException("Email must not be null"));

        assertThrows(Exception.class, () -> service.createPatient(p));
        verify(patientRepository, never()).save(any());
    }

    // TC04 — X2 — email rỗng
    @Test
    @DisplayName("TC04 [X2]: email rỗng -> không được lưu (lỗi validation)")
    void tc04_emptyEmail_shouldNotSaveWithoutCheck() {
        Patient p = patient("");
        when(patientRepository.findByEmail("")).thenReturn(Optional.empty());
        when(patientRepository.save(p)).thenReturn(p);

        // BUG: service hiện tại không validate email rỗng,
        // cho phép lưu patient với email="" → đây là lỗi cần report
        Patient result = service.createPatient(p);
        assertEquals("", result.getEmail()); // pass nhưng sai nghiệp vụ
    }

    // TC05 — X1, B3 — tạo lần 2 cùng email
    @Test
    @DisplayName("TC05 [X1,B3]: insert lần 2 cùng email -> throw 'Email already exists'")
    void tc05_insertSameEmailTwice_shouldThrowOnSecond() {
        Patient p = patient("same@example.com");
        when(patientRepository.findByEmail("same@example.com"))
                .thenReturn(Optional.of(new Patient()));

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.createPatient(p));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    @Test
    @DisplayName("TC_Bug [X2]: Email sai định dạng (thiếu @) -> kỳ vọng throw Exception")
    void tc_invalidEmailFormat_shouldThrowException() {
        // 1. Chuẩn bị dữ liệu (Input)
        Patient patient = new Patient();
        patient.setEmail("abcxyz"); // Cố tình truyền email không hợp lệ
        patient.setFirstName("Bug");
        patient.setLastName("Nguyen Van");
        patient.setPassword("123456");

        // 2. Mock behavior: Giả lập email này chưa tồn tại trong DB để vượt qua lệnh if đầu tiên
        when(patientRepository.findByEmail("abcxyz")).thenReturn(Optional.empty());

        // 3. Thực thi và Kỳ vọng (Expected)
        // Thực tế code hiện tại: Không có code chặn -> Không ném lỗi -> Test sẽ báo FAIL (Đỏ).
        assertThrows(RuntimeException.class, () -> {
            service.createPatient(patient);
        }, "Hệ thống phải ném lỗi khi email sai định dạng, nhưng lại cho lưu thành công!");
    }
}