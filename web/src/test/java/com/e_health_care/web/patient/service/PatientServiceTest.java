package com.e_health_care.web.patient.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
@Epic("Patient Management")
@Feature("Patient Lookup")
class PatientServiceTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService service;

    @Test
    @Story("Lấy danh sách toàn bộ bệnh nhân")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống trả về đúng danh sách bệnh nhân khi repository có dữ liệu, thông tin email của từng bệnh nhân phải khớp với dữ liệu lưu trong cơ sở dữ liệu.")
    void getAllPatients_shouldReturnList() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("patient@test.com");
        when(patientRepository.findAll()).thenReturn(List.of(p));

        List<Patient> result = service.getAllPatients();

        assertEquals(1, result.size());
        assertEquals("patient@test.com", result.get(0).getEmail());
    }

    @Test
    @Story("Lấy danh sách bệnh nhân khi không có dữ liệu")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra hệ thống trả về danh sách rỗng khi chưa có bệnh nhân nào trong cơ sở dữ liệu, không gây lỗi ngoại lệ.")
    void getAllPatients_shouldReturnEmptyList_whenNoPatientsExist() {
        when(patientRepository.findAll()).thenReturn(List.of());

        List<Patient> result = service.getAllPatients();

        assertEquals(0, result.size());
    }

    @Test
    @Story("Tìm bệnh nhân theo ID đã tồn tại")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống trả về đúng đối tượng Patient khi truy vấn theo ID hợp lệ đã tồn tại trong cơ sở dữ liệu.")
    void getPatientById_shouldReturnPatient_whenFound() {
        Patient p = new Patient();
        p.setId(1L);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));

        Optional<Patient> result = service.getPatientById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @Story("Tìm bệnh nhân theo ID không tồn tại")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra hệ thống trả về Optional rỗng khi truy vấn bệnh nhân theo ID không tồn tại trong cơ sở dữ liệu, không gây lỗi ngoại lệ.")
    void getPatientById_shouldReturnEmpty_whenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Patient> result = service.getPatientById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    @Story("Tìm bệnh nhân theo email đã tồn tại")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống trả về đúng đối tượng Patient khi truy vấn theo email hợp lệ đã được đăng ký trong cơ sở dữ liệu.")
    void getPatientByEmail_shouldReturnPatient_whenFound() {
        Patient p = new Patient();
        p.setEmail("patient@test.com");
        when(patientRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(p));

        Optional<Patient> result = service.getPatientByEmail("patient@test.com");

        assertTrue(result.isPresent());
        assertEquals("patient@test.com", result.get().getEmail());
    }

    @Test
    @Story("Tìm bệnh nhân theo email không tồn tại")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra hệ thống trả về Optional rỗng khi truy vấn bệnh nhân theo email không tồn tại trong cơ sở dữ liệu, không gây lỗi ngoại lệ.")
    void getPatientByEmail_shouldReturnEmpty_whenNotFound() {
        when(patientRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        Optional<Patient> result = service.getPatientByEmail("notfound@test.com");

        assertFalse(result.isPresent());
    }
}