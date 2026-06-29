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

    @Test
    @DisplayName("TC01 [V1,V2,B2]: email chưa tồn tại -> tạo patient thành công")
    void tc01_newEmail_shouldCreatePatient() {
        Patient p = patient("new@example.com");
        when(patientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(patientRepository.save(p)).thenReturn(p);

        Patient result = service.createPatient(p);

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    @DisplayName("TC02 [X1,B3]: email đã tồn tại -> throw 'Email already exists'")
    void tc02_duplicateEmail_shouldThrow() {
        Patient p = patient("dup@example.com");
        when(patientRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(new Patient()));

        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    @Test
    @DisplayName("TC03 [X2]: email null -> throw exception validation")
    void tc03_nullEmail_shouldThrow() {
        Patient p = patient(null);
        // Dev đã fix chặn null ngay đầu hàm, không cần mock repository nữa
        assertThrows(RuntimeException.class, () -> service.createPatient(p));
    }

    @Test
    @DisplayName("TC04 [X2]: email rỗng -> bị chặn bởi validation (BUG ĐÃ FIX)")
    void tc04_emptyEmail_shouldNotSaveWithoutCheck() {
        Patient p = patient("");
        // Dev đã fix, hệ thống giờ sẽ ném lỗi chứ không cho lưu nữa
        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertNotNull(ex);
    }

    @Test
    @DisplayName("TC_Bug [X2]: Email sai định dạng -> bị chặn (BUG ĐÃ FIX)")
    void tc_invalidEmailFormat_shouldThrowException() {
        Patient patient = new Patient();
        patient.setEmail("abcxyz");

        // Dev đã fix chặn format, không cần mock repository nữa
        assertThrows(RuntimeException.class, () -> service.createPatient(patient));
    }

    @Test
    @DisplayName("TC05 [X1,B3]: insert lần 2 cùng email -> throw 'Email already exists'")
    void tc05_insertSameEmailTwice_shouldThrowOnSecond() {
        Patient p = patient("same@example.com");
        when(patientRepository.findByEmail("same@example.com")).thenReturn(Optional.of(new Patient()));

        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }
}