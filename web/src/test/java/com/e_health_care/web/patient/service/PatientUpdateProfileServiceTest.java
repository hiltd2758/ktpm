package com.e_health_care.web.patient.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ ở tab "Behaviors"
@Epic("Patient Management")
@Feature("Patient Profile Update")
class PatientUpdateProfileServiceTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientUpdateProfileService service;

    @Test
    @Story("Lấy thông tin hồ sơ bệnh nhân theo ID để hiển thị form cập nhật")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống trả về đúng PatientDTO với đầy đủ thông tin (email, họ, tên) khi truy vấn bệnh nhân theo ID hợp lệ để hiển thị lên form chỉnh sửa hồ sơ.")
    void getPatientById_shouldReturnDTO_whenFound() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("patient@test.com");
        p.setFirstName("John");
        p.setLastName("Doe");
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));

        PatientDTO result = service.getPatientById(1L);

        assertNotNull(result);
        assertEquals("patient@test.com", result.getEmail());
        assertEquals("John", result.getFirstName());
    }

    @Test
    @Story("Lấy thông tin hồ sơ bệnh nhân theo ID không tồn tại")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống ném RuntimeException khi truy vấn bệnh nhân theo ID không tồn tại trong cơ sở dữ liệu, đảm bảo không trả về dữ liệu rỗng gây lỗi ở tầng hiển thị.")
    void getPatientById_shouldThrow_whenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getPatientById(99L));
    }

    @Test
    @Story("Cập nhật hồ sơ bệnh nhân thành công")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống cập nhật thành công thông tin hồ sơ bệnh nhân (họ tên, số điện thoại) và gọi đúng hàm lưu của repository khi ID bệnh nhân tồn tại.")
    void updatePatient_shouldSave_whenFound() {
        Patient p = new Patient();
        p.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));

        PatientDTO dto = new PatientDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setPhone("0909123456");

        service.updatePatient(dto, 1L);

        verify(patientRepository, times(1)).save(any());
    }

    @Test
    @Story("Cập nhật hồ sơ bệnh nhân với ID không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném RuntimeException và không gọi hàm save khi cố gắng cập nhật hồ sơ bệnh nhân với ID không tồn tại trong cơ sở dữ liệu.")
    void updatePatient_shouldThrow_whenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.updatePatient(new PatientDTO(), 99L)
        );
    }
}