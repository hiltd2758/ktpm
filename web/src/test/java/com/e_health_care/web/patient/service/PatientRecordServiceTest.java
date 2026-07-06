package com.e_health_care.web.patient.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.model.PatientClinicalInfor;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ ở tab "Behaviors"
@Epic("Patient Management")
@Feature("Patient Clinical Record")
class PatientRecordServiceTest extends BaseServiceTest {

    @Mock
    private PatientClinicalInforRepository inforRepository;

    @InjectMocks
    private PatientRecordService service;

    @Test
    @Story("Lấy thông tin lâm sàng để chỉnh sửa khi bệnh nhân đã có dữ liệu")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống trả về đúng DTO thông tin lâm sàng (nhóm máu, dị ứng, bệnh mãn tính, tiền sử gia đình) khi bệnh nhân đã có dữ liệu lâm sàng trong cơ sở dữ liệu.")
    void getClinicalForEdit_shouldReturnDTO_whenFound() {
        PatientClinicalInfor info = new PatientClinicalInfor();
        info.setBloodType("A+");
        info.setAllergies("None");
        info.setChronicDiseases("None");
        info.setFamilyMedicalHistory("None");

        when(inforRepository.findByPatientId(1L)).thenReturn(Optional.of(info));

        PatientClinicalInforDTO result = service.getClinicalForEdit(1L);

        assertNotNull(result);
        assertEquals("A+", result.getBloodType());
    }

    @Test
    @Story("Lấy thông tin lâm sàng để chỉnh sửa khi bệnh nhân chưa có dữ liệu")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống trả về DTO rỗng mang đúng patientId khi bệnh nhân chưa có dữ liệu lâm sàng nào được lưu, đảm bảo form chỉnh sửa vẫn hiển thị được mà không gây lỗi null.")
    void getClinicalForEdit_shouldReturnEmptyDTO_whenNotFound() {
        when(inforRepository.findByPatientId(99L)).thenReturn(Optional.empty());

        PatientClinicalInforDTO result = service.getClinicalForEdit(99L);

        assertNotNull(result);
        assertEquals(99L, result.getId());
    }
}