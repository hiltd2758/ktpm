package com.e_health_care.web.admin.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ ở tab "Behaviors"
@Epic("Admin Management")
@Feature("Admin Update Patient")
class AdminUpdatePatientTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AdminManagementService service;

    // TC01 — V1, V2, B1 — Cập nhật thông tin hợp lệ
    @Test
    @DisplayName("TC01 [V1, V2, B1]: Cập nhật thông tin hợp lệ -> thành công")
    @Story("Cập nhật thông tin bệnh nhân với dữ liệu hợp lệ")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra admin cập nhật thành công thông tin bệnh nhân khi ID tồn tại và dữ liệu hợp lệ, hệ thống gọi đúng hàm save và trả về đối tượng Patient với ID khớp.")
    void tc01_updatePatient_validInfo_shouldSucceed() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("valid@example.com");
        p.setFirstName("Old");

        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.save(p)).thenReturn(p);

        Patient result = service.updatePatient(p);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(patientRepository).save(p);
    }

    // TC02 — X1 — Bệnh nhân không tồn tại
    @Test
    @DisplayName("TC02 [X1]: Bệnh nhân không tồn tại -> throw 'Patient not found'")
    @Story("Cập nhật thông tin bệnh nhân với ID không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném RuntimeException với thông báo 'Patient not found' và không gọi hàm save khi admin cố cập nhật thông tin bệnh nhân với ID không tồn tại trong cơ sở dữ liệu.")
    void tc02_updatePatient_notFound_shouldThrow() {
        Patient p = new Patient();
        p.setId(999L);
        p.setEmail("notfound@example.com");

        when(patientRepository.existsById(999L)).thenReturn(false);

        Exception ex = assertThrows(RuntimeException.class, () -> service.updatePatient(p));
        assertEquals("Patient not found", ex.getMessage());

        verify(patientRepository, never()).save(any());
    }

    // TC03_Bug — X2, B2 — Cập nhật email trùng với ID của người khác
    @Test
    @DisplayName("TC03_Bug [X2, B2]: Cập nhật email trùng với ID của người khác -> kỳ vọng throw Exception")
    @Story("Cập nhật email bệnh nhân bị trùng với tài khoản khác (BUG)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném RuntimeException khi admin cập nhật email của bệnh nhân thành một email đã thuộc về tài khoản bệnh nhân khác. BUG: hàm updatePatient thiếu kiểm tra findByEmail — test hiện FAIL đỏ, cần Dev bổ sung validate trùng email khi cập nhật.")
    void tc03_updatePatient_duplicateEmail_shouldThrowException() {
        // Bệnh nhân 1 (Người đang được update)
        Patient patientToUpdate = new Patient();
        patientToUpdate.setId(1L);
        patientToUpdate.setEmail("taken@example.com");

        // Bệnh nhân 2 (Người đang sở hữu email taken@example.com)
        Patient existingPatientInDb = new Patient();
        existingPatientInDb.setId(2L);
        existingPatientInDb.setEmail("taken@example.com");

        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(existingPatientInDb));

        // BUG: Hàm updatePatient thiếu kiểm tra findByEmail nên test sẽ FAIL đỏ.
        assertThrows(RuntimeException.class, () -> {
            service.updatePatient(patientToUpdate);
        }, "Lỗi bảo mật: Hệ thống không chặn khi cập nhật email trùng với tài khoản khác!");
    }
}