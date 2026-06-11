package com.e_health_care.web.patient.service.dto;

import com.e_health_care.web.BvaValidationHelper;
import com.e_health_care.web.patient.dto.AppointmentRequestDTO;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentRequestDTOBvaTest {

    private AppointmentRequestDTO createValidDTO() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setPatientId(1L);
        dto.setDoctorId(1L);
        dto.setScheduleTime(LocalDateTime.now().plusDays(1));
        return dto;
    }

    @Test
    void patientId_null_shouldBeInvalid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setPatientId(null);
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void patientId_min_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setPatientId(1L);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void patientId_nominal_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setPatientId(500L);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void patientId_max_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setPatientId(Long.MAX_VALUE);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void doctorId_null_shouldBeInvalid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setDoctorId(null);
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void doctorId_min_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setDoctorId(1L);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void doctorId_nominal_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setDoctorId(500L);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void doctorId_max_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setDoctorId(Long.MAX_VALUE);
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void scheduleTime_null_shouldBeInvalid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setScheduleTime(null);
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void scheduleTime_past_shouldBeInvalid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setScheduleTime(LocalDateTime.now().minusDays(1));
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void scheduleTime_present_shouldBeInvalid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setScheduleTime(LocalDateTime.now());
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void scheduleTime_nearFuture_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setScheduleTime(LocalDateTime.now().plusMinutes(5));
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void scheduleTime_farFuture_shouldBeValid() {
        AppointmentRequestDTO dto = createValidDTO();
        dto.setScheduleTime(LocalDateTime.now().plusDays(30));
        assertTrue(BvaValidationHelper.isValid(dto));
    }
}