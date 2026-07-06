package com.e_health_care.web.doctor.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.e_health_care.web.doctor.dto.DoctorDTO;
import com.e_health_care.web.doctor.model.Doctor;
import com.e_health_care.web.doctor.repository.DoctorRepository;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@ExtendWith(MockitoExtension.class)
// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ ở tab "Behaviors"
@Epic("Doctor Management")
@Feature("Doctor Profile & Lookup")
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    @Story("Cập nhật hồ sơ bác sĩ thành công khi doctorId tồn tại")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra hệ thống cập nhật thành công thông tin bác sĩ và gọi hàm lưu của repository khi doctorId tồn tại và dữ liệu đầu vào hợp lệ (đầy đủ chuyên khoa, số điện thoại đúng định dạng).")
    void updateDoctorProfile_DoctorIdExists_CallsRepositorySave() {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(1L);
        dto.setFirstName("Kha");
        dto.setLastName("Nhu");
        dto.setField("Tim mạch");      // bắt buộc — validate "Chuyên khoa không được để trống"
        dto.setPhone("0987654321");     // bắt buộc — phải đúng 10 hoặc 11 ký tự

        Doctor mockDoctor = new Doctor();
        mockDoctor.setId(1L);

        when(doctorRepository.findById(any())).thenReturn(Optional.of(mockDoctor));

        doctorService.updateDoctorProfile(dto);

        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    @Story("Cập nhật hồ sơ với doctorId không tồn tại")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hệ thống ném ra RuntimeException với đúng nội dung thông báo 'Không tìm thấy bác sĩ với ID: 99' khi doctorId không tồn tại trong cơ sở dữ liệu.")
    void updateDoctorProfile_DoctorIdDoesNotExist_ThrowsException() {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(99L);

        when(doctorRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDoctorProfile(dto);
        });

        // Kiểm tra luôn câu báo lỗi có khớp với code của Dev không
        assertEquals("Không tìm thấy bác sĩ với ID: 99", exception.getMessage());
    }

    @Test
    @Story("Tìm bác sĩ theo email đã tồn tại")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hệ thống trả về đúng DoctorDTO khi tìm kiếm bằng email đã tồn tại trong cơ sở dữ liệu.")
    void getDoctorByEmail_EmailExists_ReturnsDoctorDTO() {
        // Kịch bản: Tìm bằng Email có thật -> Trả về DTO
        String email = "test@doctor.com";
        Doctor mockDoctor = new Doctor();
        mockDoctor.setId(1L);
        mockDoctor.setEmail(email);

        when(doctorRepository.findByEmail(email)).thenReturn(mockDoctor);

        DoctorDTO result = doctorService.getDoctorByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    @Story("Tìm bác sĩ theo email không tồn tại")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra hệ thống trả về null khi tìm kiếm bác sĩ bằng email không tồn tại trong cơ sở dữ liệu.")
    void getDoctorByEmail_EmailDoesNotExist_ReturnsNull() {
        // Kịch bản: Tìm Email không có thật -> Trả về null
        String email = "notfound@doctor.com";
        when(doctorRepository.findByEmail(email)).thenReturn(null);

        DoctorDTO result = doctorService.getDoctorByEmail(email);

        assertNull(result);
    }
}