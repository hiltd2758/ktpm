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

class AdminUpdatePatientTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AdminManagementService service;

    // TC01 — V1, V2, B1 — Cập nhật thông tin hợp lệ (Email giữ nguyên hoặc email mới)
    @Test
    @DisplayName("TC01 [V1, V2, B1]: Cập nhật thông tin hợp lệ -> thành công")
    void tc01_updatePatient_validInfo_shouldSucceed() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("valid@example.com");
        p.setFirstName("Old");

        // Mock: Bệnh nhân có tồn tại
        when(patientRepository.existsById(1L)).thenReturn(true);
        // Mock: Lưu thành công
        when(patientRepository.save(p)).thenReturn(p);

        Patient result = service.updatePatient(p);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(patientRepository).save(p); // Đảm bảo hàm save được gọi
    }

    // TC02 — X1 — Bệnh nhân không tồn tại
    @Test
    @DisplayName("TC02 [X1]: Bệnh nhân không tồn tại -> throw 'Patient not found'")
    void tc02_updatePatient_notFound_shouldThrow() {
        Patient p = new Patient();
        p.setId(999L); // ID không có thật
        p.setEmail("notfound@example.com");

        // Mock: Bệnh nhân không tồn tại
        when(patientRepository.existsById(999L)).thenReturn(false);

        Exception ex = assertThrows(RuntimeException.class, () -> service.updatePatient(p));
        assertEquals("Patient not found", ex.getMessage());

        // Đảm bảo tuyệt đối hàm save KHÔNG được gọi
        verify(patientRepository, never()).save(any());
    }

    // TC03_Bug — X2, B2 — Cập nhật email trùng với ID của người khác
    @Test
    @DisplayName("TC03_Bug [X2, B2]: Cập nhật email trùng với ID của người khác -> kỳ vọng throw Exception")
    void tc03_updatePatient_duplicateEmail_shouldThrowException() {
        // Bệnh nhân 1 (Người đang được update)
        Patient patientToUpdate = new Patient();
        patientToUpdate.setId(1L);
        patientToUpdate.setEmail("taken@example.com");

        // Bệnh nhân 2 (Người đang sở hữu email taken@example.com)
        Patient existingPatientInDb = new Patient();
        existingPatientInDb.setId(2L);
        existingPatientInDb.setEmail("taken@example.com");

        // Mock
        when(patientRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(existingPatientInDb));

        // BUG: Hàm updatePatient thiếu kiểm tra findByEmail nên test sẽ FAIL đỏ.
        assertThrows(RuntimeException.class, () -> {
            service.updatePatient(patientToUpdate);
        }, "Lỗi bảo mật: Hệ thống không chặn khi cập nhật email trùng với tài khoản khác!");
    }
}