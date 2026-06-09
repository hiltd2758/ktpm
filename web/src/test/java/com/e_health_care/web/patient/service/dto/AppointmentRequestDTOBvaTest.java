package com.e_health_care.web.patient.service.dto;

import com.e_health_care.web.BvaValidationHelper;
import com.e_health_care.web.patient.dto.AppointmentRequestDTO;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentRequestDTOBvaTest {

    @Test
    void patientId_null_shouldBeInvalid() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setDoctorId(1L); // Các trường khác phải hợp lệ
        dto.setScheduleTime(LocalDateTime.now().plusDays(1));

        dto.setPatientId(null); // Trường bị lỗi
        // Lưu ý: Nhấn Alt + Enter vào chữ BvaValidationHelper để máy tự import nha
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void patientId_valid_shouldBeValid() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setDoctorId(1L);
        dto.setScheduleTime(LocalDateTime.now().plusDays(1));
        dto.setPatientId(1L);

        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void doctorId_null_shouldBeInvalid() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setPatientId(1L);
        dto.setScheduleTime(LocalDateTime.now().plusDays(1));

        dto.setDoctorId(null);
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void scheduleTime_null_shouldBeInvalid() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(1L);

        dto.setScheduleTime(null);
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void scheduleTime_past_shouldBeInvalid() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(1L);

        dto.setScheduleTime(LocalDateTime.now().minusDays(1)); // Quá khứ
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void scheduleTime_future_shouldBeValid() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(1L);

        dto.setScheduleTime(LocalDateTime.now().plusDays(1)); // Tương lai
        assertTrue(BvaValidationHelper.isValid(dto));
    }
}