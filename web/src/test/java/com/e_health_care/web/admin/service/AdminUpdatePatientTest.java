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
@Feature("Update Patient")
class AdminUpdatePatientTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AdminManagementService service;

    @Test
    @Story("Cập nhật bệnh nhân thành công")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Cập nhật thành công khi bệnh nhân tồn tại và email giữ nguyên hoặc đổi sang email chưa ai dùng.")
    @DisplayName("TC01 [V1, V2, B1]: Cập nhật thông tin hợp lệ -> thành công")
    void tc01_updatePatient_validInfo_shouldSucceed() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("valid@example.com");

        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.findByEmail("valid@example.com")).thenReturn(Optional.of(p));
        when(patientRepository.save(p)).thenReturn(p);

        Patient result = service.updatePatient(p);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(patientRepository).save(p);
    }

    @Test
    @Story("Thất bại do ID không tồn tại")
    @Severity(SeverityLevel.NORMAL)
    @Description("Hệ thống ném lỗi 'Patient not found' khi cố cập nhật thông tin cho một ID không có trong cơ sở dữ liệu.")
    @DisplayName("TC02 [X1]: Bệnh nhân không tồn tại -> throw 'Patient not found'")
    void tc02_updatePatient_notFound_shouldThrow() {
        Patient p = new Patient();
        p.setId(999L);

        when(patientRepository.existsById(999L)).thenReturn(false);

        Exception ex = assertThrows(RuntimeException.class, () -> service.updatePatient(p));
        assertEquals("Patient not found", ex.getMessage());

        verify(patientRepository, never()).save(any());
    }

    @Test
    @Story("Thất bại do cập nhật email trùng lặp")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Chặn hành động lưu và ném lỗi khi email mới cập nhật đã thuộc quyền sở hữu của một bệnh nhân khác (Bug EHC-57 đã fix).")
    @DisplayName("TC03_Bug [V1, X2, B2]: Cập nhật email trùng với ID người khác -> throw Exception")
    void tc03_updatePatient_duplicateEmail_shouldThrowException() {
        Patient patientToUpdate = new Patient();
        patientToUpdate.setId(1L);
        patientToUpdate.setEmail("taken@example.com");

        Patient existingPatientInDb = new Patient();
        existingPatientInDb.setId(2L); // ID khác sở hữu email này
        existingPatientInDb.setEmail("taken@example.com");

        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(existingPatientInDb));

        Exception ex = assertThrows(RuntimeException.class, () -> service.updatePatient(patientToUpdate));
        assertTrue(ex.getMessage().contains("Email already exists"));

        verify(patientRepository, never()).save(any());
    }
}