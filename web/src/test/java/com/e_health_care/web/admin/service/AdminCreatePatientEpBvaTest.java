package com.e_health_care.web.admin.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import com.e_health_care.web.patient.repository.PatientRepository;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * EP + BVA cho createPatient() — AdminManagementService
 *
 * V1 = email chưa tồn tại    X1 = email đã tồn tại
 * V2 = patient object hợp lệ X2 = email null/rỗng
 * B2 = insert lần đầu (hợp lệ)
 * B3 = insert lần 2 cùng email (biên trùng)
 */
// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ ở tab "Behaviors"
@Epic("Admin Management")
@Feature("Admin Create Patient")
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
    @Story("Tạo tài khoản bệnh nhân mới với email hợp lệ chưa tồn tại")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra admin tạo thành công tài khoản bệnh nhân mới khi email chưa tồn tại trong hệ thống, đồng thời xác minh repository lưu đúng dữ liệu và trả về đối tượng Patient.")
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
    @Story("Tạo tài khoản bệnh nhân với email đã tồn tại trong hệ thống")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném RuntimeException với thông báo 'Email already exists' và không lưu dữ liệu khi admin cố tạo tài khoản bệnh nhân với email đã được đăng ký trước đó.")
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
    @Story("Tạo tài khoản bệnh nhân với email null")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống ném RuntimeException với thông báo 'Email must not be blank' và không gọi repository khi admin tạo tài khoản bệnh nhân mà không cung cấp email (null).")
    void tc03_nullEmail_shouldThrow() {
        Patient p = patient(null);

        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertEquals("Email must not be blank", ex.getMessage());
        verify(patientRepository, never()).save(any());
    }

    // TC04 — X2 — email rỗng -> throw "Email must not be blank"
    @Test
    @DisplayName("TC04 [X2]: email rỗng -> throw 'Email must not be blank'")
    @Story("Tạo tài khoản bệnh nhân với email rỗng")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống ném RuntimeException với thông báo 'Email must not be blank' và không gọi repository khi admin tạo tài khoản bệnh nhân với email là chuỗi rỗng.")
    void tc04_emptyEmail_shouldThrow() {
        Patient p = patient("");

        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertEquals("Email must not be blank", ex.getMessage());
        verify(patientRepository, never()).save(any());
    }

    // TC05 — X1, B3 — tạo lần 2 cùng email
    @Test
    @DisplayName("TC05 [X1,B3]: insert lần 2 cùng email -> throw 'Email already exists'")
    @Story("Tạo tài khoản bệnh nhân trùng email lần thứ hai (kiểm tra biên trùng lặp)")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên: hệ thống ném RuntimeException với thông báo 'Email already exists' khi cố tạo lần thứ hai với cùng email đã tồn tại, xác minh tính nhất quán của validate trùng email.")
    void tc05_insertSameEmailTwice_shouldThrowOnSecond() {
        Patient p = patient("same@example.com");
        when(patientRepository.findByEmail("same@example.com"))
                .thenReturn(Optional.of(new Patient()));

        Exception ex = assertThrows(RuntimeException.class,
                () -> service.createPatient(p));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    // TC06 — X2 RESOLVED — email sai định dạng
    @Test
    @DisplayName("TC06 [X2 - RESOLVED]: email sai định dạng (thiếu @) -> throw 'Invalid email format'")
    @Story("Tạo tài khoản bệnh nhân với email sai định dạng")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném RuntimeException với thông báo 'Invalid email format' khi admin nhập email không đúng định dạng (thiếu ký tự @). DEFECT đã được fix — validate định dạng email xảy ra trước khi truy vấn repository.")
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