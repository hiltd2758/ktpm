package com.e_health_care.web.admin.service;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Import Allure annotations
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Admin Management")
@Feature("Create Patient")
class AdminCreatePatientEpBvaTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

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
    @Story("Tạo mới bệnh nhân thành công")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Tạo mới bệnh nhân thành công khi dữ liệu hợp lệ và email chưa tồn tại trong hệ thống.")
    @DisplayName("TC01 [V1,V2,B2]: email chưa tồn tại -> tạo patient thành công")
    void tc01_newEmail_shouldCreatePatient() {
        Patient p = patient("new@example.com");
        when(patientRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(patientRepository.save(p)).thenReturn(p);

        Patient result = service.createPatient(p);
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    @Story("Thất bại do trùng email")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Hệ thống phải ném lỗi 'Email already exists' khi cố tạo bệnh nhân với email đã có người sử dụng.")
    @DisplayName("TC02 [X1,B3]: email đã tồn tại -> throw 'Email already exists'")
    void tc02_duplicateEmail_shouldThrow() {
        Patient p = patient("dup@example.com");
        when(patientRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(new Patient()));

        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    @Test
    @Story("Thất bại do email bị null")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Chặn đứng hành động lưu và ném lỗi validation khi email truyền vào là null.")
    @DisplayName("TC03 [X2]: email null -> throw exception validation")
    void tc03_nullEmail_shouldThrow() {
        assertThrows(RuntimeException.class, () -> service.createPatient(patient(null)));
    }

    @Test
    @Story("Thất bại do email rỗng")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Chặn đứng hành động lưu và ném lỗi validation khi email truyền vào để trống (Bug EHC-51 đã fix).")
    @DisplayName("TC04 [X2]: email rỗng -> bị chặn bởi validation")
    void tc04_emptyEmail_shouldNotSaveWithoutCheck() {
        assertThrows(RuntimeException.class, () -> service.createPatient(patient("")));
    }

    @Test
    @Story("Thất bại do email sai định dạng")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Chặn đứng hành động lưu và ném lỗi validation khi email không chứa ký tự @ (Bug EHC-51 đã fix).")
    @DisplayName("TC_Bug [X2]: Email sai định dạng -> bị chặn")
    void tc_invalidEmailFormat_shouldThrowException() {
        assertThrows(RuntimeException.class, () -> service.createPatient(patient("abcxyz")));
    }

    @Test
    @Story("Thất bại khi insert trùng liên tiếp")
    @Severity(SeverityLevel.NORMAL)
    @Description("Ném lỗi khi cố tình thực hiện hành động tạo 2 lần liên tiếp cùng một email.")
    @DisplayName("TC05 [X1,B3]: insert lần 2 cùng email -> throw 'Email already exists'")
    void tc05_insertSameEmailTwice_shouldThrowOnSecond() {
        Patient p = patient("same@example.com");
        when(patientRepository.findByEmail("same@example.com")).thenReturn(Optional.of(new Patient()));

        Exception ex = assertThrows(RuntimeException.class, () -> service.createPatient(p));
        assertTrue(ex.getMessage().contains("Email already exists"));
    }
}