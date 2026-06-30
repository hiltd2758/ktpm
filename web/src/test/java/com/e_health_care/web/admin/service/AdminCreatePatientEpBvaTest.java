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

    // TC03 — X2 — email null -> throw "Email must not be blank"
    @Test
    @DisplayName("TC03 [X2]: email null -> throw 'Email must not be blank'")
    void tc03_nullEmail_shouldThrow() {
        Patient p = patient(null);

        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertEquals("Email must not be blank", ex.getMessage());
        verify(patientRepository, never()).save(any());
    }

    // TC04 — X2 — email rỗng -> throw "Email must not be blank"
    @Test
    @DisplayName("TC04 [X2]: email rỗng -> throw 'Email must not be blank'")
    void tc04_emptyEmail_shouldThrow() {
        Patient p = patient("");

        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertEquals("Email must not be blank", ex.getMessage());
        verify(patientRepository, never()).save(any());
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
    // TC_Bug -> đã RESOLVED, đổi tên và sửa lại kỳ vọng đúng theo code thật
    @Test
    @DisplayName("TC06 [X2 - RESOLVED]: email sai định dạng (thiếu @) -> throw 'Invalid email format'")
    void tc06_invalidEmailFormat_shouldThrow() {
        Patient patient = new Patient();
        patient.setEmail("abcxyz");
        patient.setFirstName("Bug");
        patient.setLastName("Nguyen Van");
        patient.setPassword("123456");

        // Không cần mock findByEmail vì code throw trước khi gọi tới nó

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.createPatient(patient));
        assertEquals("Invalid email format", ex.getMessage());
    }
}