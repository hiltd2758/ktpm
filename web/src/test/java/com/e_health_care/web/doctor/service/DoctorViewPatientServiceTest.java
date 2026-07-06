package com.e_health_care.web.doctor.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.e_health_care.web.BaseServiceTest;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;
import com.e_health_care.web.patient.dto.PatientClinicalInforDTO;
import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.model.PatientClinicalInfor;
import com.e_health_care.web.patient.repository.PatientClinicalInforRepository;
import com.e_health_care.web.patient.repository.PatientRepository;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@MockitoSettings(strictness = Strictness.LENIENT)
// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ ở tab "Behaviors"
@Epic("Doctor Management")
@Feature("Doctor View Patient Records")
class DoctorViewPatientServiceTest extends BaseServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientClinicalInforRepository patientClinicalInforRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorViewPatientService service;

    @Test
    @Story("Lấy danh sách toàn bộ bệnh nhân")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống trả về đúng danh sách bệnh nhân khi bác sĩ truy vấn toàn bộ danh sách, dữ liệu trả về khớp với thông tin trong repository.")
    void getAllPatients_shouldReturnList() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("patient@test.com");
        p.setFirstName("John");
        p.setLastName("Doe");

        when(patientRepository.findAll()).thenReturn(List.of(p));

        var result = service.getAllPatients();

        assertEquals(1, result.size());
        assertEquals("patient@test.com", result.get(0).getEmail());
    }

    @Test
    @Story("Lấy danh sách bệnh nhân khi không có dữ liệu")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra hệ thống trả về danh sách rỗng khi không có bệnh nhân nào trong cơ sở dữ liệu, không gây lỗi ngoại lệ.")
    void getAllPatients_shouldReturnEmptyList_whenNoPatientsExist() {
        when(patientRepository.findAll()).thenReturn(List.of());
        var result = service.getAllPatients();
        assertEquals(0, result.size());
    }

    @Test
    @Story("Xem hồ sơ bệnh nhân theo ID đã tồn tại")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống truy vấn đúng hồ sơ bệnh nhân từ repository khi bác sĩ xem thông tin bệnh nhân theo ID hợp lệ.")
    void getPatientProfile_shouldReturnDTO_whenFound() {
        Patient p = new Patient();
        p.setId(1L);
        p.setEmail("patient@test.com");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));

        PatientDTO result = service.getPatientProfile(1L);

        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    @Story("Xem hồ sơ bệnh nhân theo ID không tồn tại")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống trả về null khi bác sĩ tra cứu hồ sơ bệnh nhân với ID không tồn tại trong cơ sở dữ liệu.")
    void getPatientProfile_shouldReturnNull_whenNotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        PatientDTO result = service.getPatientProfile(99L);

        assertNull(result);
    }

    @Test
    @Story("Xem thông tin lâm sàng bệnh nhân đã tồn tại")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống trả về đúng thông tin lâm sàng (ví dụ nhóm máu) của bệnh nhân khi dữ liệu đã tồn tại trong cơ sở dữ liệu.")
    void getPatientClinicalInfo_shouldReturnDTO_whenFound() {
        PatientClinicalInfor info = new PatientClinicalInfor();
        info.setId(1L);
        info.setBloodType("B+");

        Patient p = new Patient();
        p.setId(1L);
        info.setPatient(p);

        when(patientClinicalInforRepository.findByPatientId(1L)).thenReturn(Optional.of(info));

        PatientClinicalInforDTO result = service.getPatientClinicalInfo(1L);

        assertNotNull(result);
        assertEquals("B+", result.getBloodType());
    }

    @Test
    @Story("Xem thông tin lâm sàng bệnh nhân không tồn tại")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra hệ thống trả về null khi truy vấn thông tin lâm sàng của bệnh nhân không tồn tại trong cơ sở dữ liệu.")
    void getPatientClinicalInfo_shouldReturnNull_whenNotFound() {
        when(patientClinicalInforRepository.findByPatientId(99L)).thenReturn(Optional.empty());

        PatientClinicalInforDTO result = service.getPatientClinicalInfo(99L);

        assertNull(result);
    }

    @Test
    @Story("Cập nhật thông tin lâm sàng khi bác sĩ không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném ra UsernameNotFoundException khi email của bác sĩ thực hiện cập nhật không tồn tại trong hệ thống, đảm bảo chỉ bác sĩ hợp lệ mới được thao tác.")
    void updatePatientClinicalInfo_shouldThrow_whenDoctorNotFound() {
        when(doctorRepository.findByEmail("notfound@test.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () ->
                service.updatePatientClinicalInfo(1L, new com.e_health_care.web.patient.dto.PatientClinicalInforDTO(), "notfound@test.com")
        );
    }

    @Test
    @Story("Cập nhật thông tin lâm sàng khi bệnh nhân không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném ra RuntimeException khi bác sĩ hợp lệ cố gắng cập nhật thông tin lâm sàng cho bệnh nhân có ID không tồn tại trong cơ sở dữ liệu.")
    void updatePatientClinicalInfo_shouldThrow_whenPatientNotFound() {
        Doctor doctor = new Doctor();
        when(doctorRepository.findByEmail("doctor@test.com")).thenReturn(doctor);
        when(patientClinicalInforRepository.findByPatientId(99L)).thenReturn(Optional.empty());
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.updatePatientClinicalInfo(99L, new com.e_health_care.web.patient.dto.PatientClinicalInforDTO(), "doctor@test.com")
        );
    }

    @Test
    @Story("Cập nhật thông tin lâm sàng thành công")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống lưu thành công thông tin lâm sàng của bệnh nhân khi bác sĩ và bệnh nhân đều hợp lệ, đồng thời gọi đúng hàm save của repository.")
    void updatePatientClinicalInfo_shouldSave_whenValid() {
        Doctor doctor = new Doctor();
        when(doctorRepository.findByEmail("doctor@test.com")).thenReturn(doctor);

        PatientClinicalInfor existing = new PatientClinicalInfor();
        Patient p = new Patient();
        p.setId(1L);
        existing.setPatient(p);

        when(patientClinicalInforRepository.findByPatientId(1L)).thenReturn(Optional.of(existing));

        service.updatePatientClinicalInfo(1L, new com.e_health_care.web.patient.dto.PatientClinicalInforDTO(), "doctor@test.com");

        verify(patientClinicalInforRepository, times(1)).save(any());
    }
}